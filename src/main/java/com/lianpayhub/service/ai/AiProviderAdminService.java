package com.lianpayhub.service.ai;

import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.common.error.ErrorCode;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.repository.AiProviderConfigRepository;
import com.lianpayhub.service.ai.provider.AiProviderRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
public class AiProviderAdminService {
    private final AiProviderConfigRepository repository;
    private final AiProviderRegistry registry;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiProviderAdminService(AiProviderConfigRepository repository, AiProviderRegistry registry,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AiProviderConfig> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AiProviderConfig detail(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI平台不存在"));
    }

    @Transactional
    public AiProviderConfig create(String providerCode, String displayName, String baseUrl, String consoleBaseUrl, String configJson, String credentialJson) {
        String safeProviderCode = normalizeProviderCode(providerCode);
        registry.require(safeProviderCode);
        if (repository.findByProviderCode(safeProviderCode).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "AI平台编码已存在");
        }
        return repository.save(new AiProviderConfig(safeProviderCode, displayName, baseUrl, consoleBaseUrl, configJson, credentialJson));
    }

    @Transactional
    public AiProviderConfig update(Long id, String displayName, String baseUrl, String consoleBaseUrl, String configJson, String credentialJson) {
        AiProviderConfig config = detail(id);
        config.update(displayName, baseUrl, consoleBaseUrl, configJson, credentialJson);
        return repository.save(config);
    }

    @Transactional
    public AiProviderConfig changeStatus(Long id, boolean enabled) {
        AiProviderConfig config = detail(id);
        config.changeEnabled(enabled);
        return repository.save(config);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(detail(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> moacodePricing(Long id) {
        AiProviderConfig config = requireMoacode(id);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Map<String, Object> modelsRaw = getJson(accountBaseUrl(config) + "/api/v1/public/models", null);
        Map<String, Object> providersRaw;
        try {
            providersRaw = getJson(accountBaseUrl(config) + "/api/v1/public/providers", null);
        } catch (BusinessException ex) {
            providersRaw = new LinkedHashMap<String, Object>();
        }
        Map<String, Object> availableProvidersRaw = new LinkedHashMap<String, Object>();
        String cookie = credential(config, "usageCookie", "forwardKey", "cookie");
        if (cookie != null) {
            try {
                availableProvidersRaw = getJson(accountBaseUrl(config) + "/api/v1/user/available-providers", cookie);
            } catch (BusinessException ex) {
                availableProvidersRaw = new LinkedHashMap<String, Object>();
            }
        }
        Map<String, Object> pricingIndex = moacodePricingIndex(rows(modelsRaw, "models"), rows(providersRaw, "providers"),
                availableProvidersRaw, config.getProviderCode());
        result.put("providerCode", config.getProviderCode());
        result.put("billingContext", pricingIndex.get("billingContext"));
        result.put("billingContextCandidates", pricingIndex.get("billingContextCandidates"));
        result.put("models", pricingIndex.get("models"));
        result.put("providers", rows(providersRaw, "providers"));
        Map<String, Object> raw = new LinkedHashMap<String, Object>();
        raw.put("models", modelsRaw);
        raw.put("providers", providersRaw);
        raw.put("availableProviders", availableProvidersRaw);
        result.put("raw", raw);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> moacodeUsage(Long id) {
        AiProviderConfig config = requireMoacode(id);
        String cookie = credential(config, "usageCookie", "forwardKey", "cookie");
        if (cookie == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MoaCode Cookie 未配置，无法查询余额和用量");
        }
        String baseUrl = accountBaseUrl(config);
        if ("moacode-team".equalsIgnoreCase(config.getProviderCode())) {
            Map<String, Object> team = getJson(baseUrl + "/api/v1/user/team?include_member_costs=false", cookie);
            Map<String, Object> usage = getJson(baseUrl + "/api/v1/user/team/usage?period=1month", cookie);
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("providerCode", config.getProviderCode());
            result.put("balanceSummary", teamBalanceSummary(team));
            result.put("usageSummary", teamUsageSummary(usage));
            result.put("raw", raw("team", team, "usage", usage));
            return result;
        }
        Map<String, Object> balance = getJson(baseUrl + "/api/v1/user/balance", cookie);
        Map<String, Object> usage = getJson(baseUrl + "/api/v1/user/usage/summary", cookie);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("providerCode", config.getProviderCode());
        result.put("balanceSummary", balanceSummary(balance));
        result.put("usageSummary", usageSummary(usage));
        result.put("raw", raw("balance", balance, "usage", usage));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> accountBalance(Long id) {
        AiProviderConfig config = detail(id);
        if ("deepseek".equalsIgnoreCase(config.getProviderCode())) {
            return deepSeekAccountBalance(config);
        }
        if ("moacode".equalsIgnoreCase(config.getProviderCode())
                || "moacode-team".equalsIgnoreCase(config.getProviderCode())) {
            Map<String, Object> usage = moacodeUsage(id);
            Map<String, Object> balanceSummary = map(usage.get("balanceSummary"));
            Object available = "moacode-team".equalsIgnoreCase(config.getProviderCode())
                    ? firstText(value(balanceSummary, "effectiveAvailableBalance"), value(balanceSummary, "teamDailyRemainingBalance"),
                            value(balanceSummary, "dailyRemainingBalance"), value(balanceSummary, "userDailyRemainingBalance"))
                    : firstText(value(balanceSummary, "totalBalance"), value(balanceSummary, "balance"),
                            value(balanceSummary, "subscriptionBalance"), value(balanceSummary, "payAsYouGoBalance"));
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            result.put("providerCode", config.getProviderCode());
            result.put("keyStatus", "active");
            result.put("currencyUnit", "moacode-team".equalsIgnoreCase(config.getProviderCode()) ? "moacode_team_balance" : "moacode_balance");
            result.put("availableBalance", numberOrValue(available));
            result.put("balanceSummary", balanceSummary);
            result.put("usageSummary", usage.get("usageSummary"));
            result.put("raw", usage.get("raw"));
            result.put("fetchedAt", System.currentTimeMillis());
            return result;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "该 AI 平台暂不支持余额查询");
    }

    private Map<String, Object> deepSeekAccountBalance(AiProviderConfig config) {
        String apiKey = credential(config, "adminApiKey", "modelApiKey", "apiKey");
        if (apiKey == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "DeepSeek API Key 未配置，无法查询余额");
        }
        String baseUrl = firstText(config.getBaseUrl(), "https://api.deepseek.com/v1");
        baseUrl = baseUrl.replaceAll("/+$", "");
        Map<String, Object> raw = getJsonWithBearer(baseUrl + "/user/balance", apiKey);
        List<Map<String, Object>> balanceInfos = rows(data(raw), "balance_infos");
        double total = 0;
        for (Map<String, Object> info : balanceInfos) {
            Double amount = number(firstText(value(info, "total_balance"), value(info, "granted_balance")));
            if (amount != null) total += amount;
        }
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("balanceInfos", balanceInfos);
        summary.put("totalBalance", total);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("providerCode", config.getProviderCode());
        result.put("keyStatus", "active");
        result.put("currencyUnit", "CNY");
        result.put("availableBalance", total);
        result.put("balanceSummary", summary);
        result.put("raw", raw);
        result.put("fetchedAt", System.currentTimeMillis());
        return result;
    }

    private String normalizeProviderCode(String providerCode) {
        if (providerCode == null || providerCode.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI平台编码不能为空");
        }
        return providerCode.trim().toLowerCase();
    }

    private AiProviderConfig requireMoacode(Long id) {
        AiProviderConfig config = detail(id);
        if (!"moacode".equalsIgnoreCase(config.getProviderCode())
                && !"moacode-team".equalsIgnoreCase(config.getProviderCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该接口仅支持 MoaCode 平台");
        }
        return config;
    }

    private String accountBaseUrl(AiProviderConfig config) {
        String baseUrl = firstText(config.getConsoleBaseUrl(), config.getBaseUrl(), "https://api.moacode.com");
        baseUrl = baseUrl.replaceAll("/+$", "");
        return baseUrl.replaceAll("(?i)/v1$", "");
    }

    private Map<String, Object> getJson(String url, String cookie) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            if (cookie != null && !cookie.trim().isEmpty()) {
                headers.add(HttpHeaders.COOKIE, cookie.trim());
            }
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<Void>(headers), String.class);
            String body = response.getBody() == null ? "{}" : response.getBody();
            return objectMapper.readValue(body, Map.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "MoaCode 查询失败: " + ex.getMessage());
        }
    }

    private Map<String, Object> getJsonWithBearer(String url, String apiKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(apiKey.trim());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<Void>(headers), String.class);
            String body = response.getBody() == null ? "{}" : response.getBody();
            return objectMapper.readValue(body, Map.class);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "AI 平台余额查询失败: " + ex.getMessage());
        }
    }

    private Map<String, Object> moacodePricingIndex(List<Map<String, Object>> modelRows, List<Map<String, Object>> providerRows,
                                                    Map<String, Object> availableProvidersRaw, String providerCode) {
        Map<String, Object> availableData = data(availableProvidersRaw);
        Map<String, Object> priorityRoot = map(availableData.get("provider_priority_by_context"));
        List<String> billingContextCandidates = moacodeBillingContextCandidates(providerCode);
        Map<String, List<Map<String, Object>>> priorityEntriesByContext = new LinkedHashMap<String, List<Map<String, Object>>>();
        String billingContext = null;
        for (String candidate : billingContextCandidates) {
            Map<String, Object> contextPriority = map(priorityRoot.get(candidate));
            List<Map<String, Object>> priorityEntries = new ArrayList<Map<String, Object>>();
            for (Map.Entry<String, Object> entry : contextPriority.entrySet()) {
                List<Integer> providerIds = new ArrayList<Integer>();
                if (entry.getValue() instanceof List) {
                    for (Object item : (List<?>) entry.getValue()) {
                        Double number = number(item);
                        if (number != null) providerIds.add(number.intValue());
                    }
                }
                if (!providerIds.isEmpty()) {
                    Map<String, Object> priority = new LinkedHashMap<String, Object>();
                    priority.put("type", entry.getKey());
                    priority.put("providerIds", providerIds);
                    priorityEntries.add(priority);
                }
            }
            if (!priorityEntries.isEmpty()) {
                billingContext = candidate;
                priorityEntriesByContext.put(candidate, priorityEntries);
                break;
            }
        }
        Map<Integer, Map<String, Object>> providerById = new LinkedHashMap<Integer, Map<String, Object>>();
        for (Map<String, Object> row : providerRows) {
            Double id = number(value(row, "id"));
            if (id == null) continue;
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("id", id.intValue());
            item.put("type", firstText(value(row, "type")));
            item.put("name", firstText(value(row, "name")));
            item.put("displayName", firstText(value(row, "display_name"), value(row, "displayName")));
            providerById.put(id.intValue(), item);
        }
        Map<String, List<Map<String, Object>>> byModel = new LinkedHashMap<String, List<Map<String, Object>>>();
        Map<String, Map<String, Object>> selectedByModel = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> row : modelRows) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("modelName", firstText(value(row, "model_name"), value(row, "modelName"), value(row, "name")));
            item.put("displayName", firstText(value(row, "display_name"), value(row, "displayName"), value(row, "model_name")));
            item.put("providerName", firstText(value(row, "provider_name"), value(row, "providerName")));
            item.put("providerDisplay", firstText(value(row, "provider_display"), value(row, "providerDisplay")));
            item.put("providerId", number(value(row, "provider_id")) == null ? null : number(value(row, "provider_id")).intValue());
            item.put("rateMultiplier", value(row, "rate_multiplier"));
            item.put("inputTokenPrice", value(row, "input_token_price"));
            item.put("outputTokenPrice", value(row, "output_token_price"));
            item.put("cacheCreationTokenPrice", value(row, "cache_creation_token_price"));
            item.put("cacheReadTokenPrice", value(row, "cache_read_token_price"));
            item.put("requestPrice", value(row, "request_price"));
            String normalizedModel = String.valueOf(item.get("modelName")).toLowerCase();
            List<Map<String, Object>> entries = byModel.get(normalizedModel);
            if (entries == null) {
                entries = new ArrayList<Map<String, Object>>();
                byModel.put(normalizedModel, entries);
            }
            entries.add(item);
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : byModel.entrySet()) {
            Map<String, Object> selected = null;
            List<Map<String, Object>> priorityEntries = priorityEntriesByContext.get(billingContext);
            if (priorityEntries != null) {
                for (Map<String, Object> priority : priorityEntries) {
                    Object providerIdsValue = priority.get("providerIds");
                    if (!(providerIdsValue instanceof List)) continue;
                    for (Object providerIdValue : (List<?>) providerIdsValue) {
                        Double providerId = number(providerIdValue);
                        if (providerId == null) continue;
                        Map<String, Object> candidate = null;
                        for (Map<String, Object> pricing : entry.getValue()) {
                            Double rowProviderId = number(pricing.get("providerId"));
                            if (rowProviderId != null && rowProviderId.intValue() == providerId.intValue()) {
                                candidate = pricing;
                                break;
                            }
                        }
                        if (candidate == null) continue;
                        Map<String, Object> provider = providerById.get(providerId.intValue());
                        String expectedType = String.valueOf(priority.get("type"));
                        if (provider == null || expectedType.equals(provider.get("type"))) {
                            selected = candidate;
                            break;
                        }
                    }
                    if (selected != null) break;
                }
            }
            if (selected == null && !entry.getValue().isEmpty()) {
                selected = entry.getValue().get(0);
            }
            if (selected != null) {
                selectedByModel.put(entry.getKey(), selected);
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : byModel.entrySet()) {
            Map<String, Object> selected = selectedByModel.get(entry.getKey());
            for (Map<String, Object> row : entry.getValue()) {
                Map<String, Object> item = new LinkedHashMap<String, Object>(row);
                item.put("billingContext", billingContext);
                item.put("billingSelected", selected != null && selected.get("providerId") != null && selected.get("providerId").equals(row.get("providerId")));
                item.put("billingProviderId", selected == null ? null : selected.get("providerId"));
                item.put("billingProviderDisplay", selected == null ? null : selected.get("providerDisplay"));
                item.put("billingRateMultiplier", selected == null ? null : selected.get("rateMultiplier"));
                result.add(item);
            }
        }
        Map<String, Object> index = new LinkedHashMap<String, Object>();
        index.put("billingContext", billingContext);
        index.put("billingContextCandidates", billingContextCandidates);
        index.put("models", result);
        return index;
    }

    private List<String> moacodeBillingContextCandidates(String providerCode) {
        if ("moacode-team".equalsIgnoreCase(providerCode)) {
            List<String> result = new ArrayList<String>();
            result.add("team");
            return result;
        }
        List<String> result = new ArrayList<String>();
        result.add("subscription");
        result.add("pay_as_you_go");
        result.add("payg");
        result.add("pay");
        return result;
    }

    private Map<String, Object> balanceSummary(Map<String, Object> raw) {
        Map<String, Object> data = data(raw);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        copy(result, data, "balance", "balance");
        copy(result, data, "totalBalance", "total_balance", "totalBalance");
        copy(result, data, "subscriptionBalance", "subscription_balance", "subscriptionBalance");
        copy(result, data, "payAsYouGoBalance", "pay_as_you_go_balance", "payAsYouGoBalance");
        copy(result, data, "weeklyLimit", "weekly_limit", "weeklyLimit");
        copy(result, data, "weeklySpentBalance", "weekly_spent_balance", "weeklySpentBalance");
        copy(result, data, "balancePreference", "balance_preference", "balancePreference");
        return result;
    }

    private Map<String, Object> usageSummary(Map<String, Object> raw) {
        Map<String, Object> data = data(raw);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        copy(result, data, "firstRequestAt", "first_request_at", "firstRequestAt");
        copy(result, data, "lastRequestAt", "last_request_at", "lastRequestAt");
        copy(result, data, "totalRequests", "total_requests", "totalRequests");
        copy(result, data, "totalInputTokens", "total_input_tokens", "totalInputTokens");
        copy(result, data, "totalOutputTokens", "total_output_tokens", "totalOutputTokens");
        copy(result, data, "totalCacheCreationTokens", "total_cache_creation_tokens", "totalCacheCreationTokens");
        copy(result, data, "totalCacheReadTokens", "total_cache_read_tokens", "totalCacheReadTokens");
        copy(result, data, "totalCost", "total_cost", "totalCost");
        result.put("models", modelUsageRows(data.get("model_stats")));
        return result;
    }

    private Map<String, Object> teamBalanceSummary(Map<String, Object> raw) {
        Map<String, Object> data = data(raw);
        Map<String, Object> team = map(data.get("team"));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        copy(result, data, "hasTeam", "has_team", "hasTeam");
        copy(result, data, "isOwner", "is_owner", "isOwner");
        copy(result, data, "role", "role");
        result.put("teamId", firstText(value(team, "id"), value(data, "team_id")));
        result.put("teamName", firstText(value(team, "name"), value(data, "team_name")));
        copy(result, data, "dailyBalance", "daily_balance", "dailyBalance");
        copy(result, data, "dailyRemainingBalance", "daily_remaining_balance", "dailyRemainingBalance");
        copy(result, data, "userDailyBalance", "user_daily_balance", "userDailyBalance");
        copy(result, data, "userDailyRemainingBalance", "user_daily_remaining_balance", "userDailyRemainingBalance");
        result.put("teamDailyBalance", firstText(value(data, "team_daily_balance"), value(team, "daily_balance")));
        copy(result, data, "teamDailyRemainingBalance", "team_daily_remaining_balance", "teamDailyRemainingBalance");
        result.put("weeklyLimit", firstText(value(data, "weekly_limit"), value(team, "weekly_limit")));
        copy(result, data, "currentWeekSpend", "current_week_spend", "currentWeekSpend");
        copy(result, data, "teamWeekSpend", "team_week_spend", "teamWeekSpend");
        result.put("monthlyLimit", firstText(value(data, "monthly_limit"), value(team, "monthly_limit")));
        copy(result, data, "currentMonthSpend", "current_month_spend", "currentMonthSpend");
        result.put("teamMonthlyLimit", firstText(value(data, "team_monthly_limit"), value(team, "monthly_limit")));
        copy(result, data, "teamMonthSpend", "team_month_spend", "teamMonthSpend");
        result.put("effectiveAvailableBalance", effectiveTeamBalance(result));
        return result;
    }

    private Map<String, Object> teamUsageSummary(Map<String, Object> raw) {
        Map<String, Object> data = data(raw);
        List<Map<String, Object>> models = teamUsageModelRows(data.get("team_stats"));
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        copy(result, data, "period", "period");
        result.put("totalRequests", sum(models, "requests"));
        result.put("totalInputTokens", sum(models, "inputTokens"));
        result.put("totalOutputTokens", sum(models, "outputTokens"));
        result.put("totalCacheCreationTokens", sum(models, "cacheCreationTokens"));
        result.put("totalCacheReadTokens", sum(models, "cacheReadTokens"));
        result.put("totalCost", sum(models, "cost"));
        Object memberStats = data.get("member_stats");
        result.put("memberCount", memberStats instanceof List ? ((List<?>) memberStats).size() : 0);
        result.put("models", models);
        return result;
    }

    private List<Map<String, Object>> teamUsageModelRows(Object value) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object itemValue : (List<?>) value) {
            Map<String, Object> row = map(itemValue);
            String model = firstText(value(row, "model"), value(row, "model_name"));
            if (model == null) continue;
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("model", model);
            item.put("requests", firstText(value(row, "requests"), 0));
            item.put("inputTokens", firstText(value(row, "input_tokens"), value(row, "inputTokens"), 0));
            item.put("outputTokens", firstText(value(row, "output_tokens"), value(row, "outputTokens"), 0));
            item.put("cacheCreationTokens", firstText(value(row, "cache_creation_tokens"), value(row, "cacheCreationTokens"), 0));
            item.put("cacheReadTokens", firstText(value(row, "cache_read_tokens"), value(row, "cacheReadTokens"), 0));
            item.put("cost", firstText(value(row, "total_cost"), value(row, "cost"), 0));
            result.add(item);
        }
        return result;
    }

    private Double sum(List<Map<String, Object>> rows, String field) {
        double total = 0;
        for (Map<String, Object> row : rows) {
            Object value = row.get(field);
            if (value == null) continue;
            try {
                total += Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return total;
    }

    private Double effectiveTeamBalance(Map<String, Object> balance) {
        List<Double> candidates = new ArrayList<Double>();
        addNumber(candidates, balance.get("teamDailyRemainingBalance"));
        addNumber(candidates, balance.get("dailyRemainingBalance"));
        addNumber(candidates, balance.get("userDailyRemainingBalance"));
        addRemaining(candidates, balance.get("weeklyLimit"), balance.get("teamWeekSpend"));
        addRemaining(candidates, balance.get("weeklyLimit"), balance.get("currentWeekSpend"));
        addRemaining(candidates, balance.get("teamMonthlyLimit"), balance.get("teamMonthSpend"));
        addRemaining(candidates, balance.get("monthlyLimit"), balance.get("currentMonthSpend"));
        if (candidates.isEmpty()) return null;
        double min = candidates.get(0);
        for (Double candidate : candidates) {
            if (candidate < min) min = candidate;
        }
        return min;
    }

    private void addRemaining(List<Double> values, Object limit, Object spent) {
        Double safeLimit = number(limit);
        Double safeSpent = number(spent);
        if (safeLimit != null && safeSpent != null) values.add(Math.max(0, safeLimit - safeSpent));
    }

    private void addNumber(List<Double> values, Object value) {
        Double number = number(value);
        if (number != null) values.add(number);
    }

    private Double number(Object value) {
        if (value == null) return null;
        try {
            double parsed = Double.parseDouble(String.valueOf(value));
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Map<String, Object>> modelUsageRows(Object value) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!(value instanceof Map)) {
            return result;
        }
        Map<?, ?> stats = (Map<?, ?>) value;
        for (Map.Entry<?, ?> entry : stats.entrySet()) {
            Map<String, Object> row = map(entry.getValue());
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("model", String.valueOf(entry.getKey()));
            item.put("requests", value(row, "requests"));
            item.put("inputTokens", value(row, "input_tokens"));
            item.put("outputTokens", value(row, "output_tokens"));
            item.put("cacheCreationTokens", value(row, "cache_creation_tokens"));
            item.put("cacheReadTokens", value(row, "cache_read_tokens"));
            item.put("cost", value(row, "cost"));
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> rows(Map<String, Object> raw, String field) {
        Object data = raw.get("data");
        Map<String, Object> root = data instanceof Map ? map(data) : raw;
        Object value = root.get(field);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!(value instanceof List)) {
            return result;
        }
        for (Object item : (List<?>) value) {
            result.add(map(item));
        }
        return result;
    }

    private Map<String, Object> data(Map<String, Object> raw) {
        Object data = raw.get("data");
        return data instanceof Map ? map(data) : raw;
    }

    private Map<String, Object> raw(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    private Map<String, Object> map(Object value) {
        if (value instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            Map<?, ?> source = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }
        return new LinkedHashMap<String, Object>();
    }

    private void copy(Map<String, Object> target, Map<String, Object> source, String targetKey, String... sourceKeys) {
        for (String sourceKey : sourceKeys) {
            Object value = source.get(sourceKey);
            if (value != null) {
                target.put(targetKey, value);
                return;
            }
        }
        target.put(targetKey, null);
    }

    private String credential(AiProviderConfig config, String... fields) {
        try {
            JsonNode root = objectMapper.readTree(config.getCredentialJson() == null ? "{}" : config.getCredentialJson());
            for (String field : fields) {
                String value = root.path(field).asText(null);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
            return null;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI平台凭据 JSON 格式错误");
        }
    }

    private Object value(Map<String, Object> source, String key) {
        return source.get(key);
    }

    private String firstText(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                return String.valueOf(value).trim();
            }
        }
        return null;
    }

    private Object numberOrValue(Object value) {
        Double number = number(value);
        return number == null ? value : number;
    }
}
