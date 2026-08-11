var themeOptions = ["dark", "light"];
var accentOptions = ["teal", "blue", "gold", "rose", "violet"];

var state = {
  token: localStorage.getItem("lph_token") || "",
  view: localStorage.getItem("lph_view") || "dashboard",
  pageByView: {},
  filtersByView: {},
  channelsTab: localStorage.getItem("lph_channels_tab") || "payment",
  commerceTab: localStorage.getItem("lph_commerce_tab") || "products",
  logTab: "admin-operations",
  theme: localStorage.getItem("lph_theme") || "dark",
  accent: localStorage.getItem("lph_accent") || "teal"
};

var titles = {
  dashboard: ["总览", ""],
  apps: ["APP 管理", ""],
  channels: ["平台", ""],
  commerce: ["交易", ""],
  users: ["用户管理", ""],
  bindings: ["绑定管理", ""],
  devices: ["设备管理", ""],
  members: ["会员管理", ""],
  callbacks: ["回调日志", ""],
  launches: ["启动记录", ""],
  adapter: ["适配上报", ""],
  logs: ["日志审计", ""],
  admins: ["管理员", ""],
  tools: ["调试工具", ""]
};

var displayLabels = {
  ALIPAY: "支付宝",
  WECHAT: "微信支付",
  AGGREGATE: "聚合支付",
  OTHER: "其他",
  STANDARD: "标准 APP",
  DEVICE_ONLY: "设备码 APP",
  ADAPTER: "适配上报 APP",
  MEMBERSHIP: "会员套餐",
  FEATURE: "功能套餐",
  VIP: "会员",
  AI_CREDITS: "AI 算力",
  MEMBERSHIP_DURATION: "会员时长",
  CREDIT_GRANT: "算力发放",
  MANUAL: "人工处理",
  USER: "用户",
  DEVICE: "设备",
  MOBILE: "手机号",
  MOBILE_LOGIN: "手机号登录",
  DEVICE_BIND: "设备绑定",
  ENABLED: "启用",
  DISABLED: "停用",
  ACTIVE: "有效",
  EXPIRED: "已过期",
  CANCELLED: "已取消",
  PENDING: "待处理",
  PAID: "已支付",
  FAILED: "失败",
  SUCCESS: "成功",
  PARTIAL_REFUNDED: "部分退款",
  REFUNDED: "已退款",
  BOUND: "已绑定",
  UNBOUND: "未绑定",
  VERIFIED: "已验签",
  IGNORED: "已忽略",
  RECEIVED: "已接收",
  PROCESSED: "已处理",
  ORDER_CREATED: "订单创建",
  ORDER_CLOSED: "订单关闭",
  PAYMENT_SUCCESS: "支付成功",
  PAYMENT_FAILED: "支付失败",
  REFUND_CREATED: "退款创建",
  REFUND_SUCCESS: "退款成功",
  REFUND_FAILED: "退款失败",
  SYSTEM: "系统",
  ADMIN: "管理员",
  CHANNEL: "支付渠道",
  SMS: "短信",
  EMAIL: "邮件",
  AI: "AI",
  PAYMENT: "支付",
  CAPTCHA: "验证码",
  SEARCH: "搜索",
  PACKAGE: "套餐",
  ORDER: "订单",
  "aliyun": "阿里云",
  "tencent": "腾讯云",
  "aggregate": "HTTP 聚合平台",
  "local": "本地日志",
  "smtp": "SMTP 邮箱",
  "aliyun-dm": "阿里云邮件推送",
  "tencent-ses": "腾讯云 SES",
  "sendcloud": "SendCloud",
  "mailgun": "Mailgun",
  "api2d": "API2D",
  "deepseek": "DeepSeek",
  "moacode": "MoaCode",
  "moacode-team": "MoaCode Team",
  "bocha": "博查搜索"
};

var analyticsMetricLabels = {
  ORDER_COUNT: "订单数",
  PAID_ORDER_COUNT: "支付订单数",
  PAID_AMOUNT: "支付金额",
  LAUNCH_COUNT: "启动数",
  LOGIN_COUNT: "登录数",
  REFUND_COUNT: "退款数",
  REFUND_AMOUNT: "退款金额",
  ADAPTER_REPORT_COUNT: "适配上报",
  NEW_USER_COUNT: "新增用户",
  NEW_DEVICE_COUNT: "新增设备"
};

var analyticsGranularityLabels = {
  DAY: "按日",
  MONTH: "按月",
  YEAR: "按年"
};

var paymentProviderDefaults = {
  ALIPAY: "alipay",
  WECHAT: "wechat",
  AGGREGATE: "aggregate",
  OTHER: "other"
};

var aiPlatformDefaults = {
  api2d: {
    displayName: "API2D",
    baseUrl: "https://openai.api2d.net",
    consoleBaseUrl: "https://api2d.com",
    publicId: "official-1",
    publicName: "API2D",
    publicFamily: "OpenAI",
    defaultModel: "gpt-5-mini",
    authScheme: "bearer",
    billingMode: "provider_balance",
    supportsChatCompletions: true,
    supportsStreaming: true,
    supportsImages: true,
    docs: "https://api2d-doc.apifox.cn/api-84804178"
  },
  deepseek: {
    displayName: "DeepSeek",
    baseUrl: "https://api.deepseek.com/v1",
    consoleBaseUrl: "https://platform.deepseek.com/",
    publicId: "official-deepseek",
    publicName: "DeepSeek",
    publicFamily: "DeepSeek",
    defaultModel: "deepseek-chat",
    authScheme: "bearer",
    billingMode: "provider_balance",
    supportsChatCompletions: true,
    supportsStreaming: false,
    supportsImages: false,
    docs: "https://api-docs.deepseek.com"
  },
  moacode: {
    displayName: "MoaCode",
    baseUrl: "https://api.moacode.com/v1",
    consoleBaseUrl: "",
    publicId: "moacode",
    publicName: "MoaCode",
    publicFamily: "MoaCode",
    defaultModel: "gpt-5.3-codex",
    authScheme: "bearer",
    billingMode: "internal_ledger",
    supportsChatCompletions: true,
    supportsStreaming: false,
    supportsImages: true,
    docs: ""
  },
  "moacode-team": {
    displayName: "MoaCode Team",
    baseUrl: "https://api.moacode.com/v1",
    consoleBaseUrl: "",
    publicId: "moacode-team",
    publicName: "MoaCode Team",
    publicFamily: "MoaCode",
    defaultModel: "gpt-5.3-codex",
    authScheme: "bearer",
    billingMode: "internal_ledger",
    supportsChatCompletions: true,
    supportsStreaming: false,
    supportsImages: true,
    docs: ""
  }
};

var searchPlatformDefaults = {
  bocha: {
    displayName: "博查搜索",
    baseUrl: "https://api.bochaai.com",
    consoleBaseUrl: "",
    endpointPath: "/v1/web-search",
    defaultCount: 10,
    timeoutSeconds: 30,
    freshness: "",
    credentialJson: "{}"
  }
};

var notificationDefaults = {
  SMS: {
    aliyun: {
      displayName: "阿里云短信",
      senderName: "",
      senderAddress: "",
      endpoint: "dysmsapi.aliyuncs.com",
      templateCode: "SMS_123456",
      accessKeyId: "",
      accessKeySecret: "",
      secretId: "",
      secretKey: "",
      sdkAppId: "",
      region: "cn-hangzhou",
      configJson: "{}",
      credentialJson: ""
    },
    tencent: {
      displayName: "腾讯云短信",
      senderName: "",
      senderAddress: "",
      endpoint: "sms.tencentcloudapi.com",
      templateCode: "123456",
      accessKeyId: "",
      accessKeySecret: "",
      secretId: "",
      secretKey: "",
      sdkAppId: "",
      region: "ap-guangzhou",
      configJson: '{"templateParamKeys":["code"]}',
      credentialJson: ""
    },
    aggregate: {
      displayName: "聚合短信",
      senderName: "联付中枢",
      senderAddress: "联付中枢",
      endpoint: "https://sms-provider.example.com",
      templateCode: "LOGIN_CODE",
      accessKeyId: "",
      accessKeySecret: "",
      secretId: "",
      secretKey: "",
      sdkAppId: "",
      region: "",
      configJson: '{"apiKeyHeader":"X-API-Key","extraBody":{}}',
      credentialJson: '{"apiKey":""}'
    },
    local: {
      displayName: "本地日志短信",
      senderName: "联付中枢",
      senderAddress: "联付中枢",
      endpoint: "",
      templateCode: "",
      accessKeyId: "",
      accessKeySecret: "",
      secretId: "",
      secretKey: "",
      sdkAppId: "",
      region: "",
      configJson: "{}",
      credentialJson: ""
    }
  },
  EMAIL: {
    smtp: {
      displayName: "SMTP 邮箱",
      senderName: "联付中枢",
      senderAddress: "noreply@example.com",
      endpoint: "smtp.example.com",
      configJson: '{"host":"smtp.example.com","port":465,"ssl":true,"smtpAuth":true}',
      credentialJson: '{"username":"","password":""}'
    },
    "aliyun-dm": {
      displayName: "阿里云邮件推送",
      senderName: "联付中枢",
      senderAddress: "noreply@example.com",
      endpoint: "dm.aliyuncs.com",
      configJson: '{"accountName":"","regionId":"cn-hangzhou"}',
      credentialJson: '{"accessKeyId":"","accessKeySecret":""}'
    },
    "tencent-ses": {
      displayName: "腾讯云 SES",
      senderName: "联付中枢",
      senderAddress: "noreply@example.com",
      endpoint: "ses.tencentcloudapi.com",
      configJson: '{"region":"ap-guangzhou"}',
      credentialJson: '{"secretId":"","secretKey":""}'
    },
    local: {
      displayName: "本地日志邮件",
      senderName: "联付中枢",
      senderAddress: "noreply@example.com",
      endpoint: "",
      configJson: "{}",
      credentialJson: ""
    }
  }
};

function $(id) { return document.getElementById(id); }

function labelOf(value) {
  return displayLabels[value] || value;
}

function optionOf(value) {
  return { value: value, label: labelOf(value) };
}

function providerDefaultFor(channel) {
  return paymentProviderDefaults[channel] || "";
}

function bindProviderDefault(channelId, providerId) {
  var channel = $(channelId);
  var provider = $(providerId);
  if (!channel || !provider) return;
  channel.addEventListener("change", function () {
    provider.value = providerDefaultFor(channel.value);
  });
}

function init() {
  applyLegacyView();
  if (!titles[state.view]) state.view = "dashboard";
  if (["payment", "sms", "email", "ai", "search", "storage"].indexOf(state.channelsTab) < 0) state.channelsTab = "payment";
  if (["products", "purchasePages", "orders", "refunds", "packages"].indexOf(state.commerceTab) < 0) state.commerceTab = "products";
  if (themeOptions.indexOf(state.theme) < 0) state.theme = "dark";
  if (accentOptions.indexOf(state.accent) < 0) state.accent = "teal";
  applyTheme();
  $("loginForm").addEventListener("submit", login);
  $("logoutBtn").addEventListener("click", logout);
  $("refreshBtn").addEventListener("click", renderCurrent);
  $("themeToggleBtn").addEventListener("click", toggleTheme);
  $("accentSelect").addEventListener("change", function () { setAccent($("accentSelect").value); });
  $("accentSelect").value = state.accent;
  $("modalCloseBtn").addEventListener("click", closeModal);
  $("modalMask").addEventListener("click", function (e) {
    if (e.target === $("modalMask")) closeModal();
  });
  document.addEventListener("click", handleDocumentClick);
  document.addEventListener("contextmenu", handleDocumentContextMenu);
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") closeRowMenu();
    if (e.key === "Escape") closeModal();
  });
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    btn.addEventListener("click", function () { switchView(btn.dataset.view); });
  });
  if (state.token) showApp(); else showLogin();
}

function handleDocumentClick(event) {
  var menu = $("rowContextMenu");
  if (menu && !event.target.closest("#rowContextMenu")) {
    closeRowMenu();
  }
  var row = event.target.closest("tr[data-row-actions]");
  if (!row || event.target.closest("button,a,input,select,textarea,label")) return;
  var first = rowActions(row)[0];
  if (first && first.onclick) {
    event.preventDefault();
    runInlineAction(first.onclick);
  }
}

function handleDocumentContextMenu(event) {
  var row = event.target.closest("tr[data-row-actions]");
  if (!row) return;
  var actions = rowActions(row);
  if (!actions.length) return;
  event.preventDefault();
  showRowMenu(event.clientX, event.clientY, actions);
}

function rowActions(row) {
  var html = row.getAttribute("data-row-actions") || "";
  if (!html) return [];
  var holder = document.createElement("div");
  holder.innerHTML = html;
  return Array.prototype.map.call(holder.querySelectorAll("button,a"), function (el) {
    return {
      label: (el.textContent || "").trim() || el.getAttribute("title") || "操作",
      onclick: el.getAttribute("onclick") || "",
      danger: el.classList.contains("danger")
    };
  }).filter(function (item) { return !!item.onclick; });
}

function showRowMenu(x, y, actions) {
  closeRowMenu();
  var menu = document.createElement("div");
  menu.id = "rowContextMenu";
  menu.className = "row-context-menu";
  menu.innerHTML = actions.map(function (action, index) {
    return '<button type="button" class="' + (action.danger ? "danger" : "") + '" data-index="' + index + '">' +
      escapeHtml(action.label) + '</button>';
  }).join("");
  document.body.appendChild(menu);
  menu.addEventListener("click", function (event) {
    var btn = event.target.closest("button[data-index]");
    if (!btn) return;
    var action = actions[Number(btn.getAttribute("data-index"))];
    closeRowMenu();
    if (action && action.onclick) runInlineAction(action.onclick);
  });
  var rect = menu.getBoundingClientRect();
  var left = Math.min(x, window.innerWidth - rect.width - 8);
  var top = Math.min(y, window.innerHeight - rect.height - 8);
  menu.style.left = Math.max(8, left) + "px";
  menu.style.top = Math.max(8, top) + "px";
}

function closeRowMenu() {
  var menu = $("rowContextMenu");
  if (menu) menu.remove();
}

function runInlineAction(code) {
  try {
    Function('"use strict";' + code)();
  } catch (err) {
    toast(err.message || "操作失败");
  }
}

function applyLegacyView() {
  var legacyChannels = { paymentConfigs: "payment" };
  var legacyCommerce = { packages: "packages", orders: "orders", refunds: "refunds" };
  if (legacyChannels[state.view]) {
    state.channelsTab = legacyChannels[state.view];
    state.view = "channels";
  }
  if (legacyCommerce[state.view]) {
    state.commerceTab = legacyCommerce[state.view];
    state.view = "commerce";
  }
}

function applyTheme() {
  document.documentElement.setAttribute("data-theme", state.theme);
  document.documentElement.setAttribute("data-accent", state.accent);
  localStorage.setItem("lph_theme", state.theme);
  localStorage.setItem("lph_accent", state.accent);
  var metaTheme = document.querySelector('meta[name="theme-color"]');
  if (metaTheme) metaTheme.setAttribute("content", state.theme === "dark" ? "#0e1116" : "#f5f7fb");
  var btn = $("themeToggleBtn");
  if (btn) {
    btn.title = state.theme === "dark" ? "切换浅色" : "切换深色";
    btn.setAttribute("aria-pressed", state.theme === "dark" ? "true" : "false");
  }
}

function toggleTheme() {
  state.theme = state.theme === "dark" ? "light" : "dark";
  applyTheme();
}

function setAccent(accent) {
  state.accent = accentOptions.indexOf(accent) >= 0 ? accent : "teal";
  applyTheme();
}

function api(path, options) {
  options = options || {};
  var headers = {};
  var key;
  for (key in options.headers || {}) headers[key] = options.headers[key];
  if (options.body !== undefined && !headers["Content-Type"]) headers["Content-Type"] = "application/json";
  if (state.token) headers["Authorization"] = "Bearer " + state.token;

  return fetch(path, {
    method: options.method || "GET",
    headers: headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  }).then(function (res) {
    return res.text().then(function (text) {
      var data = null;
      if (text) {
        try { data = JSON.parse(text); } catch (err) { data = null; }
      }
      if (data && data.data && data.data.mustChangePassword) {
        openMustChangePasswordModal((data && data.message) || "默认管理员密码必须先修改");
        throw new Error((data && data.message) || "默认管理员密码必须先修改");
      }
      if (isAuthExpired(res.status, data)) {
        var authMessage = (data && data.message) || "登录已过期，请重新登录";
        handleAuthExpired(authMessage);
        throw new Error(authMessage);
      }
      if (!res.ok) {
        var message = (data && data.message) || text || ("HTTP " + res.status);
        throw new Error(message);
      }
      if (data && typeof data.code !== "undefined") {
        if (data.code !== 0) throw new Error(data.message || "请求失败");
        return data.data;
      }
      return data;
    });
  });
}

function isAuthExpired(status, data) {
  return status === 401 ||
    (status === 403 && !!state.token) ||
    (data && Number(data.code) === 401) ||
    (data && Number(data.code) === 403 && !!state.token);
}

function handleAuthExpired(message) {
  if (!state.token && $("loginView") && !$("loginView").classList.contains("hidden")) {
    $("loginError").textContent = message || "登录已过期，请重新登录";
    return;
  }
  localStorage.removeItem("lph_token");
  state.token = "";
  closeModal();
  showLogin();
  $("password").value = "";
  $("loginError").textContent = message || "登录已过期，请重新登录";
}

function queryString(params) {
  var qs = [];
  var key;
  for (key in params || {}) {
    if (params[key] !== null && params[key] !== undefined && params[key] !== "") {
      qs.push(encodeURIComponent(key) + "=" + encodeURIComponent(params[key]));
    }
  }
  return qs.length ? "?" + qs.join("&") : "";
}

function exportCsv(path, filename) {
  if (!state.exportConfirmed) {
    openRiskConfirm("确认导出数据", "导出可能包含业务敏感数据，请确认用途合规。", "确认导出", function () {
      state.exportConfirmed = true;
      closeModal();
      exportCsv(path, filename);
      state.exportConfirmed = false;
    }, { reasonRequired: false });
    return;
  }
  var headers = {};
  if (state.token) headers["Authorization"] = "Bearer " + state.token;
  fetch(path, { headers: headers }).then(function (res) {
    if (res.status === 401 || res.status === 403 && !!state.token) {
      return res.text().then(function (text) {
        var data = null;
        if (text) {
          try { data = JSON.parse(text); } catch (err) { data = null; }
        }
        var message = (data && data.message) || "登录已过期，请重新登录";
        handleAuthExpired(message);
        throw new Error(message);
      });
    }
    if (!res.ok) {
      return res.text().then(function (text) { throw new Error(text || ("HTTP " + res.status)); });
    }
    return res.blob();
  }).then(function (blob) {
    var url = URL.createObjectURL(blob);
    var link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
    toast("导出已开始");
  }).catch(function (err) { toast(err.message); });
}

function toast(message) {
  var el = $("toast");
  el.textContent = message;
  el.classList.remove("hidden");
  window.clearTimeout(state.toastTimer);
  state.toastTimer = window.setTimeout(function () { el.classList.add("hidden"); }, 2400);
}

function escapeHtml(value) {
  if (value === null || value === undefined) return "";
  return String(value).replace(/[&<>"']/g, function (c) {
    return { "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#039;" }[c];
  });
}

function formatValue(value) {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "boolean") return value ? "是" : "否";
  if (isIsoDateTime(value)) return escapeHtml(formatDateTime(value));
  return escapeHtml(labelOf(value));
}

function isIsoDateTime(value) {
  return typeof value === "string" && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value);
}

function formatDateTime(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").slice(0, 16);
}

function formatDuration(seconds) {
  if (seconds === null || seconds === undefined || seconds === "") return "-";
  var total = Math.max(0, Math.round(Number(seconds) || 0));
  if (total < 60) return total + " 秒";
  var minutes = Math.floor(total / 60);
  var remainSeconds = total % 60;
  if (minutes < 60) return remainSeconds ? minutes + " 分 " + remainSeconds + " 秒" : minutes + " 分";
  var hours = Math.floor(minutes / 60);
  var remainMinutes = minutes % 60;
  return remainMinutes ? hours + " 小时 " + remainMinutes + " 分" : hours + " 小时";
}

function formatMoney(cents) {
  if (cents === null || cents === undefined || cents === "") return "-";
  return (Number(cents) / 100).toFixed(2);
}

function centsFromYuanInput(value) {
  if (value === null || value === undefined || value === "") return null;
  return Math.round(Number(value) * 100);
}

function yuanInputFromCents(value) {
  if (value === null || value === undefined || value === "") return "";
  return (Number(value) / 100).toFixed(2);
}

function badge(value) {

  var text = escapeHtml(labelOf(value));
  var cls = "badge";
  if (["ENABLED", "ACTIVE", "PAID", "SUCCESS"].indexOf(value) >= 0) cls += " ok";
  if (["PENDING", "PARTIAL_REFUNDED", "RECEIVED"].indexOf(value) >= 0) cls += " warn";
  if (["DISABLED", "CANCELLED", "FAILED", "REFUNDED"].indexOf(value) >= 0) cls += " bad";
  return '<span class="' + cls + '">' + text + '</span>';
}

function formatBytes(bytes) {
  var value = Number(bytes || 0);
  var units = ["B", "KB", "MB", "GB", "TB"];
  var i = 0;
  while (value >= 1024 && i < units.length - 1) {
    value = value / 1024;
    i += 1;
  }
  return (i === 0 ? String(value) : value.toFixed(value >= 10 ? 1 : 2)) + " " + units[i];
}

function parseJsonObject(text) {
  if (!text) return {};
  try {
    var obj = JSON.parse(text);
    return obj && typeof obj === "object" && !(obj instanceof Array) ? obj : {};
  } catch (e) {
    return {};
  }
}

function mergeJsonText(baseText, patch) {
  var obj = parseJsonObject(baseText);
  var key;
  for (key in patch || {}) {
    if (patch[key] !== null && patch[key] !== undefined && patch[key] !== "") obj[key] = patch[key];
  }
  return JSON.stringify(obj);
}

function paymentConfigBody(prefix, includeCredential) {
  var channel = $(prefix + "Channel") ? $(prefix + "Channel").value : "ALIPAY";
  var configPatch = { defaultPayMode: $(prefix + "DefaultPayMode").value };
  var credentialPatch = {};
  if (channel === "ALIPAY") {
    configPatch.sandbox = $(prefix + "Sandbox").checked;
    applyEnvPatch(configPatch, credentialPatch, prefix);
    legacyAlipayFallbackFields(configPatch, credentialPatch, prefix);
  } else if (channel === "WECHAT") {
    configPatch.merchantSerialNo = $(prefix + "SerialNo").value;
    credentialPatch.apiV3Key = $(prefix + "ApiV3Key").value.trim();
    credentialPatch.merchantPrivateKey = $(prefix + "PrivateKey").value.trim();
  } else {
    configPatch.gatewayUrl = $(prefix + "GatewayUrl").value;
    credentialPatch.apiKey = $(prefix + "ApiKey").value.trim();
    credentialPatch.signSecret = $(prefix + "SignSecret").value.trim();
  }
  var body = {
    providerCode: $(prefix + "Provider").value,
    merchantId: $(prefix + "Merchant").value,
    channelAppId: channel === "ALIPAY" ? "" : $(prefix + "ChannelApp").value,
    notifyUrl: channel === "ALIPAY" ? "" : $(prefix + "Notify").value,
    configJson: JSON.stringify(configPatch)
  };
  var hasCredentialInput = Object.keys(credentialPatch).some(function (key) { return !!credentialPatch[key]; });
  if (includeCredential || hasCredentialInput) {
    body.credentialJson = JSON.stringify(credentialPatch);
  } else {
    body.credentialJson = "";
  }
  return body;
}

function paymentNotifyTemplate(channel) {
  return "${domain}/api/payment/notify/" + channel;
}

function paymentReturnTemplate() {
  return "${domain}/pay.html";
}

function alipayGatewayDefault(sandbox) {
  return sandbox ? "https://openapi-sandbox.dl.alipaydev.com/gateway.do" : "https://openapi.alipay.com/gateway.do";
}

function pageContent(data) {
  if (!data) return [];
  if (data.content && data.content instanceof Array) return data.content;
  if (data instanceof Array) return data;
  return [];
}

function pageMeta(data) {
  if (!data || typeof data.totalElements === "undefined") return null;
  return {
    page: data.number || 0,
    size: data.size || 20,
    totalPages: data.totalPages || 1,
    totalElements: data.totalElements || 0
  };
}

function table(columns, rows) {
  var actionColumnIndex = columns.findIndex(function (c) { return c.title === "操作"; });
  var visibleColumns = columns.filter(function (c, index) { return index !== actionColumnIndex; });
  var head = visibleColumns.map(function (c) { return "<th>" + c.title + "</th>"; }).join("");
  var body = rows.map(function (row) {
    var actionsHtml = actionColumnIndex >= 0 ? renderCell(columns[actionColumnIndex], row) : "";
    var actionsAttr = actionsHtml ? ' data-row-actions="' + escapeAttr(actionsHtml) + '"' : "";
    return "<tr" + actionsAttr + ">" + visibleColumns.map(function (c) {
      var val = typeof c.render === "function" ? c.render(row) : formatValue(row[c.key]);
      return "<td>" + val + "</td>";
    }).join("") + "</tr>";
  }).join("");
  var tableClass = actionColumnIndex >= 0 ? " data-table interactive-table" : "data-table";
  return '<div class="table-wrap"><table class="' + tableClass + '"><thead><tr>' + head + "</tr></thead><tbody>" +
    (body || '<tr><td class="empty-cell" colspan="' + Math.max(visibleColumns.length, 1) + '">暂无数据</td></tr>') +
    "</tbody></table></div>";
}

function renderCell(column, row) {
  return typeof column.render === "function" ? column.render(row) : formatValue(row[column.key]);
}

function escapeAttr(value) {
  return escapeHtml(value).replace(/"/g, "&quot;");
}

function renderPager(view, meta, reloadFn) {
  if (!meta) return "";
  var prevDisabled = meta.page <= 0 ? " disabled" : "";
  var nextDisabled = meta.page >= meta.totalPages - 1 ? " disabled" : "";
  return '<div class="pager">' +
    '<div class="meta">共 ' + meta.totalElements + ' 条，当前第 ' + (meta.page + 1) + ' / ' + meta.totalPages + ' 页</div>' +
    '<div class="actions">' +
    '<button class="secondary small" ' + prevDisabled + ' onclick="' + reloadFn + '(' + (meta.page - 1) + ')">上一页</button>' +
    '<button class="secondary small" ' + nextDisabled + ' onclick="' + reloadFn + '(' + (meta.page + 1) + ')">下一页</button>' +
    '</div></div>';
}

function panel(title, body, extraClass) {
  return '<div class="panel ' + (extraClass || "") + '"><div class="panel-title"><span>' + title + '</span></div>' + body + '</div>';
}

function fieldClass(id, extraClass) {
  var safeId = String(id || "").replace(/[^a-zA-Z0-9_-]/g, "-");
  return "field field-" + safeId + (extraClass ? " " + extraClass : "");
}

function normalizeFieldMeta(metaOrType) {
  if (!metaOrType) return { required: true, type: "text", showBadge: true, placeholder: "" };
  if (typeof metaOrType === "string") return { required: true, type: metaOrType, showBadge: true, placeholder: "" };
  return {
    required: metaOrType.required !== false,
    conditional: !!metaOrType.conditional,
    hint: metaOrType.hint || "",
    type: metaOrType.type || "text",
    showBadge: metaOrType.showBadge !== false,
    placeholder: metaOrType.placeholder || ""
  };
}

function fieldLabel(label, meta) {
  var html = '<span class="field-label"><span class="field-label-text">' + label;
  if (meta.showBadge && (meta.required || meta.conditional)) {
    html += '<span class="field-required-mark" aria-hidden="true">*</span>';
  }
  html += '</span></span>';
  if (meta.hint) html += '<span class="field-hint">' + escapeHtml(meta.hint) + '</span>';
  return html;
}

function input(id, label, value, metaOrType) {
  var meta = normalizeFieldMeta(metaOrType);
  return '<label class="' + fieldClass(id) + '">' + fieldLabel(label, meta) + '<input id="' + id + '" type="' + meta.type + '" value="' + escapeHtml(value || "") + '"></label>';
}

function fieldIconLink(link) {
  if (!link) return "";
  return '<a class="field-link icon-only" href="' + escapeHtml(link) + '" target="_blank" rel="noopener" title="打开官网">↗</a>';
}

function inputWithLink(id, label, value, link, linkLabel, metaOrType) {
  var meta = normalizeFieldMeta(metaOrType);
  var extra = fieldIconLink(link);
  return '<label class="' + fieldClass(id) + '"><span class="field-label"><span class="field-label-text">' + label + (meta.showBadge && (meta.required || meta.conditional) ? '<span class="field-required-mark" aria-hidden="true">*</span>' : '') + '</span>' + extra + '</span>' + (meta.hint ? '<span class="field-hint">' + escapeHtml(meta.hint) + '</span>' : '') + '<input id="' + id + '" type="' + meta.type + '" value="' + escapeHtml(value || '') + '"></label>';
}

function textareaWithLink(id, label, value, link, linkLabel, meta) {
  meta = normalizeFieldMeta(meta);
  var extra = fieldIconLink(link);
  return '<label class="' + fieldClass(id, "textarea-field") + '"><span class="field-label"><span class="field-label-text">' + label + (meta.showBadge && (meta.required || meta.conditional) ? '<span class="field-required-mark" aria-hidden="true">*</span>' : '') + '</span>' + extra + '</span>' + (meta.hint ? '<span class="field-hint">' + escapeHtml(meta.hint) + '</span>' : '') + '<textarea id="' + id + '">' + escapeHtml(value || '') + '</textarea></label>';
}

function envValue(normal, sandboxKey, prodKey, fallbackKey, sandbox) {
  return sandbox ? (normal[sandboxKey] || normal[fallbackKey] || '') : (normal[prodKey] || normal[fallbackKey] || '');
}

function envCredentialValue(credential, sandboxKey, prodKey, fallbackKey, sandbox) {
  return sandbox ? (credential[sandboxKey] || credential[fallbackKey] || '') : (credential[prodKey] || credential[fallbackKey] || '');
}

function envTitle(sandbox) {
  return sandbox ? '当前编辑：沙箱配置' : '当前编辑：正式配置';
}

function envPrefix(sandbox) {
  return sandbox ? 'Sandbox' : 'Prod';
}

function currentAlipayLink(links, sandbox, key) {
  if (key === 'app') return sandbox ? links.sandbox : links.appId;
  return links[key];
}

function pickCurrent(name, sandboxValue, prodValue, sandbox) {
  return sandbox ? sandboxValue : prodValue;
}

function fieldLink(url, text) { return url ? '<a class="field-link" href="' + escapeHtml(url) + '" target="_blank" rel="noopener">' + escapeHtml(text) + '</a>' : ''; }

function alipayQuickLinksRow() { var l=alipayOpenLinks(); return '<div class="field-links-row">' + fieldLink(l.sandbox,'沙箱') + fieldLink(l.appId,'应用') + fieldLink(l.key,'密钥') + fieldLink(l.pid,'账号') + '</div>'; }

function noteBlock(text) { return '<div class="field-note">' + escapeHtml(text) + '</div>'; }

function sectionTitle(title) {
  return '<div class="field-section">' + escapeHtml(title) + '</div>';
}

function textarea(id, label, value, meta) {
  meta = normalizeFieldMeta(meta);
  return '<label class="' + fieldClass(id, "textarea-field") + '">' + fieldLabel(label, meta) + '<textarea id="' + id + '">' + escapeHtml(value || "") + '</textarea></label>';
}

function pageContent(data) {
  if (!data) return [];
  if (data.content && data.content instanceof Array) return data.content;
  if (data instanceof Array) return data;
  return [];
}

function smsProviderVisibility(providerCode) {
  var provider = String(providerCode || "").toLowerCase();
  return {
    isAliyun: provider === "aliyun" || provider === "aliyun-sms",
    isTencent: provider === "tencent" || provider === "tencent-sms"
  };
}

function scopedInput(id, label, value, visible, metaOrType) {
  var meta = normalizeFieldMeta(metaOrType);
  return '<label class="' + fieldClass(id, visible ? "" : "hidden") + '">' + fieldLabel(label, meta) + '<input id="' + id + '" type="' + meta.type + '" value="' + escapeHtml(value || "") + '"></label>';
}

function providerDocLinks(providerCode) {
  var key = String(providerCode || "").toLowerCase();
  return {
    smsHome: key === "aliyun" ? "https://dysms.console.aliyun.com/" : (key === "tencent" ? "https://console.cloud.tencent.com/smsv2" : ""),
    smsAccess: key === "aliyun" ? "https://ram.console.aliyun.com/manage/ak" : (key === "tencent" ? "https://console.cloud.tencent.com/cam/capi" : ""),
    smsTemplate: key === "aliyun" ? "https://dysms.console.aliyun.com/domestic/text/template" : (key === "tencent" ? "https://console.cloud.tencent.com/smsv2/csms-template" : "")
  };
}

function scopedInputWithLink(id, label, value, visible, link, metaOrType) {
  var meta = normalizeFieldMeta(metaOrType);
  return '<label class="' + fieldClass(id, visible ? "" : "hidden") + '"><span class="field-label"><span class="field-label-text">' + label + (meta.showBadge && (meta.required || meta.conditional) ? '<span class="field-required-mark" aria-hidden="true">*</span>' : '') + '</span>' + fieldIconLink(link) + '</span>' + (meta.hint ? '<span class="field-hint">' + escapeHtml(meta.hint) + '</span>' : '') + '<input id="' + id + '" type="' + meta.type + '" value="' + escapeHtml(value || "") + '"></label>';
}

function renderSmsConfigFields(prefix, item, providerCode, mode) {
  var visibility = smsProviderVisibility(providerCode);
  var editing = mode === "edit";
  var links = providerDocLinks(providerCode);
  return scopedInputWithLink(prefix + "TemplateCode", "模板编码", item.templateCode, visibility.isAliyun, links.smsTemplate, { required: visibility.isAliyun, conditional: !visibility.isAliyun, hint: visibility.isAliyun ? "阿里云发送必填；也可通过测试发送时单独传入" : "按通道需要填写" }) +
    scopedInputWithLink(prefix + "AccessKeyId", "AccessKey ID", item.accessKeyId, visibility.isAliyun, links.smsAccess, { required: visibility.isAliyun, conditional: !visibility.isAliyun }) +
    scopedInputWithLink(prefix + "AccessKeySecret", "AccessKey Secret", item.accessKeySecret, visibility.isAliyun, links.smsAccess, { required: visibility.isAliyun && !editing, conditional: !visibility.isAliyun, type: "password" }) +
    scopedInputWithLink(prefix + "SecretId", "SecretId", item.secretId, visibility.isTencent, links.smsAccess, { required: visibility.isTencent, conditional: !visibility.isTencent }) +
    scopedInputWithLink(prefix + "SecretKey", "SecretKey", item.secretKey, visibility.isTencent, links.smsAccess, { required: visibility.isTencent && !editing, conditional: !visibility.isTencent, type: "password" }) +
    scopedInputWithLink(prefix + "SdkAppId", "SDK App ID", item.sdkAppId, visibility.isTencent, links.smsHome, { required: visibility.isTencent, conditional: !visibility.isTencent }) +
    scopedInputWithLink(prefix + "Region", "地域", item.region, visibility.isTencent || visibility.isAliyun, links.smsHome, { required: false, conditional: !(visibility.isTencent || visibility.isAliyun), hint: visibility.isTencent ? "腾讯云可选，如 ap-guangzhou" : (visibility.isAliyun ? "阿里云可选，如 cn-hangzhou" : "按平台需要填写") });
}

function sectionTitle(title) {
  return '<div class="field-section">' + escapeHtml(title) + '</div>';
}

function envField(name, sandboxKey, prodKey) {
  return { sandbox: sandboxKey, prod: prodKey, name: name };
}

function pickEnvValue(normal, sandboxKey, prodKey, fallbackKey) {
  return {
    sandbox: normal[sandboxKey] || normal[fallbackKey] || '',
    prod: normal[prodKey] || normal[fallbackKey] || ''
  };
}

function pickEnvCredential(credential, sandboxKey, prodKey, fallbackKey) {
  return {
    sandbox: credential[sandboxKey] || credential[fallbackKey] || '',
    prod: credential[prodKey] || credential[fallbackKey] || ''
  };
}

function alipayOpenLinks() {
  return {
    pid: 'https://open.alipay.com/develop/manage/account',
    appId: 'https://open.alipay.com/develop/manage/app',
    key: 'https://open.alipay.com/develop/manage/appInfo',
    sandbox: 'https://openhome.alipay.com/platform/appDaily.htm'
  };
}

function renderEnvPairFields(prefix, title, sandboxField, prodField, link, linkLabel, widget, meta) {
  var render = widget === 'textarea' ? textareaWithLink : inputWithLink;
  return sectionTitle(title) +
    render(prefix + 'Sandbox' + sandboxField.name, '沙箱' + sandboxField.name, sandboxField.sandbox, link, linkLabel, meta) +
    render(prefix + 'Prod' + prodField.name, '正式' + prodField.name, prodField.prod, link, linkLabel, meta);
}

function readEnvPair(prefix, name) {
  return {
    sandbox: ($(prefix + 'Sandbox' + name) || { value: '' }).value,
    prod: ($(prefix + 'Prod' + name) || { value: '' }).value
  };
}

function applyEnvPatch(configPatch, credentialPatch, prefix) {
  var sandbox = $(prefix + 'Sandbox').checked;
  var tag = sandbox ? 'sandbox' : 'prod';
  configPatch[tag + 'AppId'] = $(prefix + 'EnvAppId').value;
  configPatch[tag + 'GatewayUrl'] = $(prefix + 'EnvGatewayUrl').value;
  configPatch[tag + 'NotifyUrl'] = $(prefix + 'EnvNotifyUrl').value;
  configPatch[tag + 'ReturnUrl'] = $(prefix + 'EnvReturnUrl').value;
  configPatch[tag + 'SignType'] = $(prefix + 'EnvSignType').value;
  credentialPatch[tag + 'MerchantPrivateKey'] = $(prefix + 'EnvPrivateKey').value.trim();
  credentialPatch[tag + 'AlipayPublicKey'] = $(prefix + 'EnvAlipayPublicKey').value.trim();
}

function legacyAlipayFallbackFields(configPatch, credentialPatch, prefix) {
  if (!configPatch.sandboxAppId && $(prefix + 'ChannelApp')) configPatch.sandboxAppId = $(prefix + 'ChannelApp').value;
  if (!configPatch.prodAppId && $(prefix + 'ChannelApp')) configPatch.prodAppId = $(prefix + 'ChannelApp').value;
}

function alipayConfigHint() {
  return '沙箱和正式环境各自独立配置，打开沙箱模式时自动切到沙箱那套。';
}

function removeLegacyAlipayFallbacks(obj) {
  delete obj.gatewayUrl; delete obj.returnUrl; delete obj.signType;
  delete obj.merchantPrivateKey; delete obj.alipayPublicKey;
}

function fieldLinkStyleNote() { return ''; }

function fieldLabelWithLink(label, meta, link, linkLabel) { return ''; }

function fieldSectionSeparator() { return ''; }

function fieldEnvTitle() { return ''; }

function paymentDocLink(channel) { return channel === 'ALIPAY' ? 'https://opendocs.alipay.com/open/291' : (channel === 'WECHAT' ? 'https://pay.weixin.qq.com/doc/v3/' : ''); }

function providerManageLink(channel) { return channel === 'ALIPAY' ? 'https://open.alipay.com/' : (channel === 'WECHAT' ? 'https://pay.weixin.qq.com/' : ''); }

function fieldLink(url, text) { return url ? '<a class="field-link" href="' + escapeHtml(url) + '" target="_blank" rel="noopener">' + escapeHtml(text) + '</a>' : ''; }

function alipayQuickLinksRow() { var l=alipayOpenLinks(); return '<div class="field-links-row">' + fieldLink(l.sandbox,'沙箱账号') + fieldLink(l.appId,'应用列表') + fieldLink(l.key,'密钥管理') + fieldLink(l.pid,'账号信息') + '</div>'; }

function noteBlock(text) { return '<div class="field-note">' + escapeHtml(text) + '</div>'; }

function hiddenNoop() { return ''; }

function helperNoop() { return ''; }

function placeholderNoop() { return ''; }

function spacerNoop() { return ''; }

function sectionNoop() { return ''; }

function textNoop() { return ''; }

function valueNoop() { return ''; }

function labelNoop() { return ''; }

function metaNoop() { return ''; }

function actionNoop() { return ''; }

function itemNoop() { return ''; }

function rowNoop() { return ''; }

function colNoop() { return ''; }

function envNoop() { return ''; }

function linkNoop() { return ''; }

function widgetNoop() { return ''; }

function modeNoop() { return ''; }

function appNoop() { return ''; }

function configNoop() { return ''; }

function credentialNoop() { return ''; }

function channelNoop() { return ''; }

function notifyNoop() { return ''; }

function returnNoop() { return ''; }

function signNoop() { return ''; }

function keyNoop() { return ''; }

function gatewayNoop() { return ''; }

function titleNoop() { return ''; }

function sandboxNoop() { return ''; }

function prodNoop() { return ''; }

function endNoop() { return ''; }

function moreNoop() { return ''; }

function miscNoop() { return ''; }

function extraNoop() { return ''; }

function tailNoop() { return ''; }

function finalNoop() { return ''; }

function noop(){return ''}

function textarea(id, label, value, meta) {
  meta = normalizeFieldMeta(meta);
  return '<label class="' + fieldClass(id, "textarea-field") + '">' + fieldLabel(label, meta) + '<textarea id="' + id + '">' + escapeHtml(value || "") + '</textarea></label>';
}

function pageContent(data) {
  if (!data) return [];
  if (data.content && data.content instanceof Array) return data.content;
  if (data instanceof Array) return data;
  return [];
}

function smsProviderVisibility(providerCode) {
  var provider = String(providerCode || "").toLowerCase();
  return {
    isAliyun: provider === "aliyun" || provider === "aliyun-sms",
    isTencent: provider === "tencent" || provider === "tencent-sms"
  };
}

function scopedInput(id, label, value, visible, metaOrType) {
  var meta = normalizeFieldMeta(metaOrType);
  return '<label class="' + fieldClass(id, visible ? "" : "hidden") + '">' + fieldLabel(label, meta) + '<input id="' + id + '" type="' + meta.type + '" value="' + escapeHtml(value || "") + '"></label>';
}

function renderSmsConfigFields(prefix, item, providerCode, mode) {
  var visibility = smsProviderVisibility(providerCode);
  var editing = mode === "edit";
  return scopedInput(prefix + "TemplateCode", "模板编码", item.templateCode, visibility.isAliyun, { required: visibility.isAliyun, conditional: !visibility.isAliyun, hint: visibility.isAliyun ? "阿里云发送必填；也可通过测试发送时单独传入" : "按通道需要填写" }) +
    scopedInput(prefix + "AccessKeyId", "AccessKey ID", item.accessKeyId, visibility.isAliyun, { required: visibility.isAliyun, conditional: !visibility.isAliyun }) +
    scopedInput(prefix + "AccessKeySecret", "AccessKey Secret", item.accessKeySecret, visibility.isAliyun, { required: visibility.isAliyun && !editing, conditional: !visibility.isAliyun, type: "password" }) +
    scopedInput(prefix + "SecretId", "SecretId", item.secretId, visibility.isTencent, { required: visibility.isTencent, conditional: !visibility.isTencent }) +
    scopedInput(prefix + "SecretKey", "SecretKey", item.secretKey, visibility.isTencent, { required: visibility.isTencent && !editing, conditional: !visibility.isTencent, type: "password" }) +
    scopedInput(prefix + "SdkAppId", "SDK App ID", item.sdkAppId, visibility.isTencent, { required: visibility.isTencent, conditional: !visibility.isTencent }) +
    scopedInput(prefix + "Region", "地域", item.region, visibility.isTencent || visibility.isAliyun, { required: false, conditional: !(visibility.isTencent || visibility.isAliyun), hint: visibility.isTencent ? "腾讯云可选，如 ap-guangzhou" : (visibility.isAliyun ? "阿里云可选，如 cn-hangzhou" : "按平台需要填写") });
}

function syncSmsProviderFields(prefix, providerCode) {
  var visibility = smsProviderVisibility(providerCode);
  [prefix + "TemplateCode", prefix + "AccessKeyId", prefix + "AccessKeySecret"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isAliyun);
  });
  [prefix + "SecretId", prefix + "SecretKey", prefix + "SdkAppId", prefix + "Region"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isTencent);
  });
}

function notificationExtraVisibility(type, providerCode) {
  var provider = String(providerCode || "").toLowerCase();
  return {
    isSmsAggregate: type === "SMS" && provider === "aggregate",
    isSmsTencent: type === "SMS" && provider === "tencent",
    isSmtp: type === "EMAIL" && provider === "smtp",
    isAliyunDm: type === "EMAIL" && provider === "aliyun-dm",
    isTencentSes: type === "EMAIL" && provider === "tencent-ses",
    isCloudEmail: type === "EMAIL" && provider !== "smtp" && provider !== "local"
  };
}

function renderSmsExtraFields(prefix, providerCode, config, credential, mode) {
  var visibility = notificationExtraVisibility("SMS", providerCode);
  return scopedInput(prefix + "TemplateParamKeys", "模板参数名", (config.templateParamKeys || []).join(","), visibility.isSmsTencent, { required: false, conditional: !visibility.isSmsTencent }) +
    scopedInput(prefix + "ApiKeyHeader", "API Key Header", config.apiKeyHeader || "X-API-Key", visibility.isSmsAggregate, { required: false, conditional: !visibility.isSmsAggregate }) +
    scopedInput(prefix + "SmsApiKey", mode === "edit" ? "聚合平台 API Key（留空不修改）" : "聚合平台 API Key", credential.apiKey || "", visibility.isSmsAggregate, { required: false, conditional: !visibility.isSmsAggregate, type: "password" });
}

function renderEmailExtraFields(prefix, providerCode, config, credential, mode) {
  var visibility = notificationExtraVisibility("EMAIL", providerCode);
  return scopedInput(prefix + "SmtpHost", "SMTP Host", config.host || "", visibility.isSmtp, { required: visibility.isSmtp, conditional: !visibility.isSmtp }) +
    scopedInput(prefix + "SmtpPort", "SMTP 端口", config.port || 465, visibility.isSmtp, { required: false, conditional: !visibility.isSmtp, type: "number" }) +
    scopedCheckbox(prefix + "SmtpSsl", "SSL", config.ssl !== false, visibility.isSmtp) +
    scopedCheckbox(prefix + "SmtpAuth", "SMTP Auth", config.smtpAuth !== false, visibility.isSmtp) +
    scopedInput(prefix + "SmtpUsername", "SMTP 用户名", credential.username || "", visibility.isSmtp, { required: false, conditional: !visibility.isSmtp }) +
    scopedInput(prefix + "SmtpPassword", mode === "edit" ? "SMTP 密码（留空不修改）" : "SMTP 密码", credential.password || "", visibility.isSmtp, { required: false, conditional: !visibility.isSmtp, type: "password" }) +
    scopedInput(prefix + "EmailRegion", "地域", config.region || config.regionId || "", visibility.isCloudEmail, { required: false, conditional: !visibility.isCloudEmail }) +
    scopedInput(prefix + "EmailAccountName", "发信地址/账号名", config.accountName || "", visibility.isAliyunDm, { required: false, conditional: !visibility.isAliyunDm }) +
    scopedInput(prefix + "EmailAccessKeyId", mode === "edit" ? "AccessKey ID（留空不修改）" : "AccessKey ID", credential.accessKeyId || "", visibility.isAliyunDm, { required: false, conditional: !visibility.isAliyunDm }) +
    scopedInput(prefix + "EmailAccessKeySecret", mode === "edit" ? "AccessKey Secret（留空不修改）" : "AccessKey Secret", credential.accessKeySecret || "", visibility.isAliyunDm, { required: false, conditional: !visibility.isAliyunDm, type: "password" }) +
    scopedInput(prefix + "EmailSecretId", mode === "edit" ? "SecretId（留空不修改）" : "SecretId", credential.secretId || "", visibility.isTencentSes, { required: false, conditional: !visibility.isTencentSes }) +
    scopedInput(prefix + "EmailSecretKey", mode === "edit" ? "SecretKey（留空不修改）" : "SecretKey", credential.secretKey || "", visibility.isTencentSes, { required: false, conditional: !visibility.isTencentSes, type: "password" });
}

function syncNotificationExtraFields(type, prefix, providerCode) {
  var visibility = notificationExtraVisibility(type, providerCode);
  [prefix + "TemplateParamKeys"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isSmsTencent);
  });
  [prefix + "ApiKeyHeader", prefix + "SmsApiKey"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isSmsAggregate);
  });
  [prefix + "SmtpHost", prefix + "SmtpPort", prefix + "SmtpUsername", prefix + "SmtpPassword"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isSmtp);
  });
  [prefix + "SmtpSsl", prefix + "SmtpAuth"].forEach(function (id) {
    var inputEl = $(id);
    var field = inputEl ? inputEl.closest("label") : null;
    if (field) field.classList.toggle("hidden", !visibility.isSmtp);
  });
  [prefix + "EmailRegion"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isCloudEmail);
  });
  [prefix + "EmailAccountName", prefix + "EmailAccessKeyId", prefix + "EmailAccessKeySecret"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isAliyunDm);
  });
  [prefix + "EmailSecretId", prefix + "EmailSecretKey"].forEach(function (id) {
    var field = document.querySelector('.field-' + id);
    if (field) field.classList.toggle("hidden", !visibility.isTencentSes);
  });
}

function setNotificationExtraDefaults(type, prefix, providerCode, config, credential) {
  var ids = {
    TemplateParamKeys: (config.templateParamKeys || []).join(","),
    ApiKeyHeader: config.apiKeyHeader || "X-API-Key",
    SmsApiKey: credential.apiKey || "",
    SmtpHost: config.host || "",
    SmtpPort: config.port || 465,
    SmtpUsername: credential.username || "",
    SmtpPassword: credential.password || "",
    EmailRegion: config.region || config.regionId || "",
    EmailAccountName: config.accountName || "",
    EmailAccessKeyId: credential.accessKeyId || "",
    EmailAccessKeySecret: credential.accessKeySecret || "",
    EmailSecretId: credential.secretId || "",
    EmailSecretKey: credential.secretKey || ""
  };
  Object.keys(ids).forEach(function (suffix) {
    var el = $(prefix + suffix);
    if (el) el.value = ids[suffix];
  });
  var ssl = $(prefix + "SmtpSsl");
  if (ssl) ssl.checked = config.ssl !== false;
  var auth = $(prefix + "SmtpAuth");
  if (auth) auth.checked = config.smtpAuth !== false;
}

function notificationBody(type, prefix) {
  var providerCode = $(prefix + "Provider").value;
  var body = {
    providerCode: providerCode,
    displayName: $(prefix + "Name").value,
    senderName: $(prefix + "SenderName").value,
    senderAddress: $(prefix + "SenderAddress").value,
    endpoint: $(prefix + "Endpoint").value,
    configJson: notificationConfigJson(type, prefix, providerCode),
    credentialJson: notificationCredentialJson(type, prefix, providerCode)
  };
  if (type === "SMS") {
    body.templateCode = $(prefix + "TemplateCode").value;
    body.accessKeyId = $(prefix + "AccessKeyId").value;
    body.accessKeySecret = $(prefix + "AccessKeySecret").value;
    body.secretId = $(prefix + "SecretId").value;
    body.secretKey = $(prefix + "SecretKey").value;
    body.sdkAppId = $(prefix + "SdkAppId").value;
    body.region = $(prefix + "Region").value;
  }
  return body;
}

function notificationConfigJson(type, prefix, providerCode) {
  if (type === "SMS") {
    if (String(providerCode).toLowerCase() === "tencent") {
      return JSON.stringify({ templateParamKeys: splitCsv($(prefix + "TemplateParamKeys").value) });
    }
    if (String(providerCode).toLowerCase() === "aggregate") {
      return JSON.stringify({ apiKeyHeader: $(prefix + "ApiKeyHeader").value });
    }
    return "{}";
  }
  if (String(providerCode).toLowerCase() === "smtp") {
    return JSON.stringify({
      host: $(prefix + "SmtpHost").value,
      port: $(prefix + "SmtpPort").value ? Number($(prefix + "SmtpPort").value) : 465,
      ssl: $(prefix + "SmtpSsl").checked,
      smtpAuth: $(prefix + "SmtpAuth").checked
    });
  }
  return JSON.stringify({
    region: $(prefix + "EmailRegion").value,
    accountName: $(prefix + "EmailAccountName").value
  });
}

function notificationCredentialJson(type, prefix, providerCode) {
  if (type === "SMS" && String(providerCode).toLowerCase() === "aggregate") {
    var smsApiKey = $(prefix + "SmsApiKey").value;
    return smsApiKey && smsApiKey.trim() ? JSON.stringify({ apiKey: smsApiKey }) : "";
  }
  if (type === "EMAIL") {
    if (String(providerCode).toLowerCase() === "smtp") {
      var username = $(prefix + "SmtpUsername").value;
      var password = $(prefix + "SmtpPassword").value;
      return (username || password) ? JSON.stringify({ username: username, password: password }) : "";
    }
    var accessKeyId = $(prefix + "EmailAccessKeyId").value;
    var accessKeySecret = $(prefix + "EmailAccessKeySecret").value;
    var secretId = $(prefix + "EmailSecretId").value;
    var secretKey = $(prefix + "EmailSecretKey").value;
    return (accessKeyId || accessKeySecret || secretId || secretKey)
      ? JSON.stringify({ accessKeyId: accessKeyId, accessKeySecret: accessKeySecret, secretId: secretId, secretKey: secretKey })
      : "";
  }
  return "";
}

function splitCsv(value) {
  return String(value || "").split(",").map(function (item) { return item.trim(); }).filter(Boolean);
}

function notificationDetailFields(item) {
  var fields = {
    "ID": item.id,
    "类型": item.channelType,
    "平台": item.providerCode,
    "名称": item.displayName,
    "发送名称": item.senderName,
    "发送地址": item.senderAddress,
    "服务地址": item.endpoint
  };
  if (item.channelType === "SMS") {
    fields["模板编码"] = item.templateCode;
    fields["AccessKey ID"] = item.accessKeyId;
    fields["SecretId"] = item.secretId;
    fields["SDK App ID"] = item.sdkAppId;
    fields["地域"] = item.region;
  }
  fields["敏感凭据"] = item.credentialConfigured ? "已配置" : "未配置";
  fields["状态"] = item.status;
  fields["创建时间"] = item.createdAt;
  fields["更新时间"] = item.updatedAt;
  return fields;
}

function renderNotificationEditFields(item) {
  var config = parseJsonObject(item.configJson || "{}");
  var credential = parseJsonObject(item.credentialJson || "{}");
  return '<div class="form-grid notification-config-form">' +
    select("notifyEditProvider", "平台", notificationProviderOptions(item.channelType, false), item.providerCode) +
    input("notifyEditName", "配置名称", item.displayName) +
    input("notifyEditSenderName", item.channelType === "SMS" ? "短信签名" : "发件名称", item.senderName) +
    input("notifyEditSenderAddress", item.channelType === "SMS" ? "发送签名/扩展码" : "发件邮箱", item.senderAddress, { required: false }) +
    input("notifyEditEndpoint", "服务地址", item.endpoint, { required: false }) +
    (item.channelType === "SMS" ? renderSmsConfigFields("notifyEdit", item, item.providerCode, "edit") + renderSmsExtraFields("notifyEdit", item.providerCode, config, credential, "edit") : renderEmailExtraFields("notifyEdit", item.providerCode, config, credential, "edit")) +
    '</div>';
}

function bindNotificationEditProvider(item) {
  var provider = $("notifyEditProvider");
  if (!provider) return;
  if (item.channelType === "SMS") syncSmsProviderFields("notifyEdit", provider.value);
  syncNotificationExtraFields(item.channelType, "notifyEdit", provider.value);
  provider.addEventListener("change", function () {
    if (item.channelType === "SMS") syncSmsProviderFields("notifyEdit", provider.value);
    syncNotificationExtraFields(item.channelType, "notifyEdit", provider.value);
  });
}


function select(id, label, values, value, meta) {
  meta = normalizeFieldMeta(meta);
  if (/(Filter|logTab)$/.test(String(id || ""))) meta.showBadge = false;
  var html = '<label class="' + fieldClass(id) + '">' + fieldLabel(label, meta) + '<select id="' + id + '">';
  html += values.map(function (item) {
    var selected = item.value === value ? " selected" : "";
    return '<option value="' + escapeHtml(item.value) + '"' + selected + '>' + escapeHtml(item.label) + '</option>';
  }).join("");
  html += "</select></label>";
  return html;
}

function checkbox(id, label, checked) {
  return '<label class="inline-check"><input id="' + id + '" type="checkbox"' + (checked ? " checked" : "") + '> ' + label + '</label>';
}

function scopedCheckbox(id, label, checked, visible) {
  return '<label class="inline-check field-' + id + (visible ? '' : ' hidden') + '"><input id="' + id + '" type="checkbox"' + (checked ? " checked" : "") + '> ' + label + '</label>';
}

function queryFilters(view) {
  return state.filtersByView[view] || {};
}

function setFilters(view, filters) {
  state.filtersByView[view] = filters || {};
}

function setPage(view, page) {
  state.pageByView[view] = page;
}

function currentPage(view) {
  return typeof state.pageByView[view] === "number" ? state.pageByView[view] : 0;
}

function showLogin() {
  $("loginView").classList.remove("hidden");
  $("appView").classList.add("hidden");
}

function showApp() {
  $("loginView").classList.add("hidden");
  $("appView").classList.remove("hidden");
  switchView(state.view);
}

function login(e) {
  e.preventDefault();
  $("loginError").textContent = "";
  api("/admin/auth/login", {
    method: "POST",
    body: { username: $("username").value.trim(), password: $("password").value }
  }).then(function (data) {
    state.token = data.token;
    localStorage.setItem("lph_token", state.token);
    showApp();
    if (data.mustChangePassword) {
      openMustChangePasswordModal("当前仍使用默认管理员密码，请先修改后继续使用后台。");
    }
  }).catch(function (err) {
    $("loginError").textContent = err.message;
  });
}

function logout() {
  localStorage.removeItem("lph_token");
  state.token = "";
  closeModal();
  showLogin();
}

function switchView(view) {
  state.view = view;
  localStorage.setItem("lph_view", view);
  window.scrollTo(0, 0);
  $("pageTitle").textContent = titles[view][0];
  $("subTitle").textContent = titles[view][1];
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    var active = btn.dataset.view === view;
    btn.classList.toggle("active", active);
    if (active) btn.setAttribute("aria-current", "page"); else btn.removeAttribute("aria-current");
  });
  Array.prototype.forEach.call(document.querySelectorAll(".view"), function (el) {
    el.classList.toggle("hidden", el.id !== view);
  });
  renderCurrent();
}

function renderCurrent() {
  var map = {
    dashboard: renderDashboard,
    apps: renderApps,
    channels: renderChannels,
    commerce: renderCommerce,
    users: renderUsers,
    bindings: renderBindings,
    devices: renderDevices,
    members: renderMembers,
    callbacks: renderCallbacks,
    launches: renderLaunches,
    adapter: renderAdapterReports,
    logs: renderLogs,
    admins: renderAdmins,
    tools: renderTools
  };
  if (!map[state.view]) return Promise.resolve();
  setBusy(true);
  var task;
  try {
    task = map[state.view]();
  } catch (err) {
    task = Promise.reject(err);
  }
  return Promise.resolve(task)
    .catch(function (err) { toast(err.message); })
    .then(function () { setBusy(false); });
}

function renderTabs(group, active, tabs) {
  return '<div class="tab-bar" role="tablist">' + tabs.map(function (tab) {
    var activeClass = tab.key === active ? " active" : "";
    return '<button class="tab-btn' + activeClass + '" type="button" role="tab" aria-selected="' +
      (tab.key === active ? "true" : "false") + '" onclick="' + tab.action + '(\'' + tab.key + '\')">' +
      escapeHtml(tab.label) + '</button>';
  }).join("") + '</div>';
}

function switchChannelsTab(tab) {
  state.channelsTab = tab;
  localStorage.setItem("lph_channels_tab", tab);
  renderCurrent();
}

function switchCommerceTab(tab) {
  state.commerceTab = tab;
  localStorage.setItem("lph_commerce_tab", tab);
  renderCurrent();
}

function renderChannels() {
  if (["payment", "sms", "email", "ai", "search", "storage"].indexOf(state.channelsTab) < 0) {
    state.channelsTab = "payment";
  }
  var bodyId = state.channelsTab === "payment" ? "paymentConfigs" :
    state.channelsTab === "sms" ? "smsConfigs" :
    state.channelsTab === "email" ? "emailConfigs" :
    state.channelsTab === "ai" ? "aiConfigs" :
    state.channelsTab === "storage" ? "storageConfigs" : "searchPlatforms";
  $("channels").innerHTML = renderTabs("channels", state.channelsTab, [
    { key: "payment", label: "支付配置", action: "switchChannelsTab" },
    { key: "sms", label: "短信配置", action: "switchChannelsTab" },
    { key: "email", label: "邮件配置", action: "switchChannelsTab" },
    { key: "ai", label: "AI平台", action: "switchChannelsTab" },
    { key: "search", label: "搜索平台", action: "switchChannelsTab" },
    { key: "storage", label: "存储", action: "switchChannelsTab" }
  ]) + '<div id="' + bodyId + '"></div>';
  if (state.channelsTab === "payment") return renderPaymentConfigs(currentPage("paymentConfigs"));
  if (state.channelsTab === "sms") return renderSmsConfigs(currentPage("smsConfigs"));
  if (state.channelsTab === "email") return renderEmailConfigs(currentPage("emailConfigs"));
  if (state.channelsTab === "ai") return renderAiPlatforms();
  if (state.channelsTab === "storage") return renderStorageConfigs();
  return renderSearchPlatforms();
}

function renderCommerce() {
  if (["products", "purchasePages", "orders", "refunds", "packages"].indexOf(state.commerceTab) < 0) {
    state.commerceTab = "products";
  }
  var bodyId = state.commerceTab;
  $("commerce").innerHTML = renderTabs("commerce", state.commerceTab, [
    { key: "products", label: "商品", action: "switchCommerceTab" },
    { key: "purchasePages", label: "购买页", action: "switchCommerceTab" },
    { key: "orders", label: "订单", action: "switchCommerceTab" },
    { key: "refunds", label: "退款", action: "switchCommerceTab" },
    { key: "packages", label: "旧套餐", action: "switchCommerceTab" }
  ]) + '<div id="' + bodyId + '"></div>';
  if (state.commerceTab === "products") return renderProducts();
  if (state.commerceTab === "purchasePages") return renderPurchasePages();
  if (state.commerceTab === "packages") return renderPackages();
  if (state.commerceTab === "orders") return renderOrders(currentPage("orders"));
  return renderRefunds(currentPage("refunds"));
}

function setBusy(busy) {
  var refreshBtn = $("refreshBtn");
  if (refreshBtn) refreshBtn.disabled = !!busy;
}

function openModal(title, body, footer) {
  $("modalTitle").textContent = title;
  $("modalBody").innerHTML = body;
  $("modalFooter").innerHTML = footer || "";
  $("modalMask").classList.remove("hidden");
}

function openSubModal(title, body, footer) {
  $("subModalTitle").textContent = title;
  $("subModalBody").innerHTML = body;
  $("subModalFooter").innerHTML = footer || "";
  $("subModalMask").classList.remove("hidden");
}

function closeSubModal() {
  $("subModalMask").classList.add("hidden");
  $("subModalBody").innerHTML = "";
  $("subModalFooter").innerHTML = "";
}

function closeModal() {
  $("modalMask").classList.add("hidden");
  $("modalBody").innerHTML = "";
  $("modalFooter").innerHTML = "";
}

function closeAllModals() {
  closeSubModal();
  closeModal();
}

function bindSubModalClose() {
  var btn = $("subModalCloseBtn");
  if (btn && !btn.dataset.bound) {
    btn.dataset.bound = "1";
    btn.addEventListener("click", closeSubModal);
  }
}

bindSubModalClose();

function closeSubModalToProduct(productId) {
  closeSubModal();
  openProductDetail(productId);
}

function openPlanEdit(planId, encoded, productId) {
  var plan = JSON.parse(decodeURIComponent(String(encoded || "{}")));
  openSubModal("编辑方案", editPlanBody(plan), '<button class="secondary" type="button" onclick="closeSubModal()">取消</button><button type="button" onclick="savePlanEdit(' + planId + ',' + productId + ')">保存</button>');
}

function editPlanBody(plan) {
  return '<div class="form-grid">' +
    input("editPlanName", "方案名", plan.planName || "") +
    input("editPlanPrice", "价格(元)", yuanInputFromCents(plan.priceCents || 0), { required: true, type: "number", hint: "单位：元" }) +
    input("editPlanOriginPrice", "原价(元)", yuanInputFromCents(plan.originalPriceCents || ""), { required: false, type: "number", hint: "单位：元" }) +
    input("editPlanDays", "时长(天)", plan.durationDays || "", { required: false, type: "number" }) +
    input("editPlanCredits", "算力量", plan.creditAmount || "", { required: false, type: "number" }) +
    input("editPlanBadge", "标签", plan.badgeText || "", { required: false }) +
    textarea("editPlanBenefits", "方案权益", plan.benefitsText || "", { required: false }) +
    input("editPlanSort", "排序", plan.sortOrder || 0, "number") +
    '</div>';
}

function openRiskConfirm(title, message, actionLabel, onConfirm, options) {
  options = options || {};
  var reasonId = options.reasonId || "riskReason";
  var body = '<div class="risk-box">' + escapeHtml(message) + '</div>';
  if (options.reason !== false) {
    body += '<div class="form-grid">' + textarea(reasonId, "操作原因", "", { required: options.reasonRequired !== false, hint: "会写入后台操作日志，便于后续审计" }) + '</div>';
  }
  openModal(title, body,
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button class="danger" type="button" onclick="confirmRiskAction()">' + escapeHtml(actionLabel || "确认") + '</button>');
  state.pendingRiskAction = function () {
    var reason = $(reasonId) ? $(reasonId).value.trim() : "";
    if (options.reasonRequired !== false && options.reason !== false && !reason) {
      toast("请填写操作原因");
      return;
    }
    onConfirm(reason);
  };
}

function confirmRiskAction() {
  if (typeof state.pendingRiskAction === "function") state.pendingRiskAction();
}

function renderDashboard() {
  var filters = queryFilters("dashboard");
  var appId = filters.appId || "";
  var granularity = filters.granularity || "DAY";
  var metricName = filters.metric || "PAID_AMOUNT";
  var periods = filters.periods || "30";
  var analyticsPath = "/admin/reports/analytics" + queryString({
    appId: appId,
    granularity: granularity,
    metric: metricName,
    periods: periods
  });
  return Promise.all([
    api("/admin/reports/overview"),
    api("/admin/reports/trend?days=14"),
    api("/admin/reports/payment-summary"),
    api(analyticsPath),
    loadApps()
  ]).then(function (res) {
    var overview = res[0], trend = res[1], summary = res[2] || {}, analytics = res[3] || {}, apps = res[4] || [];
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    var analyticsBar = '<div class="toolbar analytics-toolbar">' +
      select("analyticsAppFilter", "APP", appOptions, appId) +
      select("analyticsGranularityFilter", "粒度", analyticsGranularityOptions(), granularity) +
      select("analyticsMetricFilter", "指标", analyticsMetricOptions(), metricName) +
      input("analyticsPeriodsFilter", "周期数", periods, "number") +
      '<button class="secondary" type="button" onclick="applyAnalyticsFilter()">生成统计</button>' +
      "</div>";
    $("dashboard").innerHTML =
      '<div class="grid metrics">' +
      metric("APP", overview.appCount) +
      metric("用户", overview.userCount) +
      metric("设备", overview.deviceCount) +
      metric("会员", overview.memberCount) +
      metric("订单", overview.orderCount) +
      metric("已支付", overview.paidOrderCount) +
      metric("收入(元)", formatMoney(overview.paidAmountCents)) +
      metric("启动", overview.launchCount) +
      "</div>" +
      panel("统计分析", analyticsBar + renderAnalyticsChart(analytics), "analytics-panel") +
      '<div class="dashboard-pair">' +
        panel("近 14 天趋势", table([
          { title: "日期", key: "date" },
          { title: "订单", key: "orderCount" },
          { title: "支付订单", key: "paidOrderCount" },
          { title: "支付金额(元)", render: function (r) { return formatMoney(r.paidAmountCents); } },
          { title: "登录", key: "loginCount" },
          { title: "启动", key: "launchCount" }
        ], trend)) +
        panel("APP 收入排行", table([
          { title: "APP", key: "dimension" },
          { title: "订单", key: "orderCount" },
          { title: "支付订单", key: "paidOrderCount" },
          { title: "支付金额(元)", render: function (r) { return formatMoney(r.paidAmountCents); } }
        ], summary.byApp || [])) +
      '</div>' +
      panel("支付渠道分布", table([
        { title: "渠道", key: "dimension" },
        { title: "订单", key: "orderCount" },
        { title: "支付订单", key: "paidOrderCount" },
        { title: "支付金额(元)", render: function (r) { return formatMoney(r.paidAmountCents); } }
      ], summary.byPayChannel || []));
  });
}

function metric(label, value) {
  return '<div class="metric"><div class="label">' + label + '</div><div class="value">' + formatValue(value) + '</div><div class="metric-halo" aria-hidden="true"></div></div>';
}

function applyAnalyticsFilter() {
  setFilters("dashboard", {
    appId: $("analyticsAppFilter").value,
    granularity: $("analyticsGranularityFilter").value,
    metric: $("analyticsMetricFilter").value,
    periods: $("analyticsPeriodsFilter").value
  });
  renderDashboard();
}

function renderAnalyticsChart(data) {
  var points = data.points || [];
  var metricName = data.metric || "ORDER_COUNT";
  var label = analyticsMetricLabels[metricName] || metricName;
  var isAmount = metricName === "PAID_AMOUNT" || metricName === "REFUND_AMOUNT";
  var max = 0;
  var i;
  for (i = 0; i < points.length; i++) {
    max = Math.max(max, Number(points[i].value || 0));
  }
  var total = isAmount ? formatMoney(data.totalAmountCents || 0) : formatValue(data.totalValue || 0);
  if (!points.length) {
    return '<div class="chart-empty">暂无统计数据</div>';
  }
  var width = 720;
  var height = 220;
  var padLeft = 42;
  var padRight = 18;
  var padTop = 18;
  var padBottom = 38;
  var chartW = width - padLeft - padRight;
  var chartH = height - padTop - padBottom;
  var safeMax = max <= 0 ? 1 : max;
  var path = "";
  var area = "";
  var bars = "";
  var ticks = "";
  var lastIndex = points.length - 1;
  for (i = 0; i < points.length; i++) {
    var x = padLeft + (lastIndex <= 0 ? chartW / 2 : chartW * i / lastIndex);
    var y = padTop + chartH - chartH * Number(points[i].value || 0) / safeMax;
    path += (i === 0 ? "M" : "L") + x.toFixed(2) + " " + y.toFixed(2) + " ";
    area += (i === 0 ? "M" + x.toFixed(2) + " " + (padTop + chartH).toFixed(2) + " L" : "L") +
      x.toFixed(2) + " " + y.toFixed(2) + " ";
    var barW = Math.max(5, Math.min(26, chartW / Math.max(points.length, 1) * 0.42));
    var barH = padTop + chartH - y;
    bars += '<rect class="chart-bar" x="' + (x - barW / 2).toFixed(2) + '" y="' + y.toFixed(2) +
      '" width="' + barW.toFixed(2) + '" height="' + Math.max(1, barH).toFixed(2) + '"><title>' +
      escapeHtml(points[i].period + " / " + analyticsDisplayValue(points[i].value, isAmount)) +
      '</title></rect>';
    if (i === 0 || i === lastIndex || (points.length > 8 && i % Math.ceil(points.length / 4) === 0)) {
      ticks += '<text class="chart-tick" x="' + x.toFixed(2) + '" y="' + (height - 12) + '">' +
        escapeHtml(points[i].period) + '</text>';
    }
  }
  area += "L" + (padLeft + chartW).toFixed(2) + " " + (padTop + chartH).toFixed(2) + " Z";
  return '<div class="analytics-summary"><div><span>' + escapeHtml(label) + '</span><strong>' +
    analyticsDisplayValue(total, isAmount) + '</strong></div><div><span>范围</span><strong>' +
    escapeHtml(data.appId || "全部 APP") + '</strong></div></div>' +
    '<div class="chart-wrap"><svg class="line-chart" viewBox="0 0 ' + width + ' ' + height +
    '" role="img" aria-label="' + escapeHtml(label) + '趋势图">' +
    '<line class="chart-axis" x1="' + padLeft + '" y1="' + (padTop + chartH) + '" x2="' + (padLeft + chartW) +
    '" y2="' + (padTop + chartH) + '"></line>' +
    '<text class="chart-y" x="8" y="' + (padTop + 8) + '">' + escapeHtml(analyticsDisplayValue(max, isAmount)) + '</text>' +
    '<path class="chart-area" d="' + area + '"></path>' +
    bars +
    '<path class="chart-line" d="' + path + '"></path>' +
    ticks +
    '</svg></div>';
}

function analyticsDisplayValue(value, isAmount) {
  if (isAmount) {
    if (typeof value === "string") return value;
    return formatMoney(value);
  }
  return formatValue(value);
}

function loadApps() {
  return api("/admin/apps").then(function (rows) {
    return rows || [];
  });
}

function renderApps() {
  return loadApps().then(function (rows) {
    $("apps").innerHTML =
      panelTitleActions("APP 列表",
        '<button type="button" onclick="openAppCreate()">新建 APP</button>' +
        '<button class="secondary" type="button" onclick="exportCsv(\'/admin/exports/apps?limit=5000\', \'apps.csv\')">导出</button>') +
      table([
        { title: "ID", key: "id" },
        { title: "APP ID", key: "appId" },
        { title: "名称", key: "appName" },
        { title: "类型", key: "appType" },
        { title: "密钥版本", key: "appSecretVersion" },
        { title: "手机号登录", render: function (r) { return badge(r.needMobileLogin ? "ENABLED" : "DISABLED"); } },
        { title: "密码登录", render: function (r) { return badge(r.allowPasswordLogin ? "ENABLED" : "DISABLED"); } },
        { title: "设备会员", render: function (r) { return badge(r.needDeviceVip ? "ENABLED" : "DISABLED"); } },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openAppDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="downloadIntegrationPackage(' + r.id + ')">接入包</button>' +
              '<button class="small" onclick="openAppEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="toggleApp(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '<button class="small" onclick="resetSecret(' + r.id + ')">重置密钥</button>' +
              '<button class="small danger" onclick="deleteApp(' + r.id + ')">删除</button>' +
              '</div>';
          }
        }
      ], rows) +
      '</div>';
  });
}

function downloadIntegrationPackage(id) {
  exportCsv("/admin/apps/" + id + "/integration-package", "lianpayhub-integration-" + id + ".md");
}

function openAppCreate() {
  openModal("新建 APP", '<div class="form-grid">' +
    input("appId", "APP ID") +
    input("appName", "APP 名称") +
    select("appType", "类型", [
      optionOf("STANDARD"),
      optionOf("DEVICE_ONLY"),
      optionOf("ADAPTER")
    ], "STANDARD") +
    checkbox("needMobileLogin", "手机号登录", true) +
    checkbox("allowPasswordLogin", "密码登录", false) +
    checkbox("allowAvatarUpload", "头像上传", true) +
    input("accessTokenMinutes", "Access Token 时长(分钟)", "30", { required: false, type: "number" }) +
    input("refreshTokenMinutes", "Refresh Token 时长(分钟)", "43200", { required: false, type: "number" }) +
    checkbox("needDeviceVip", "设备会员", false) +
    checkbox("enableUserAiKey", "启用用户AI Key", false) +
    input("defaultAiQuotaUnits", "默认AI配额", "0", { required: false, type: "number" }) +
    input("defaultAiProviderCode", "默认AI平台", "api2d", { required: false }) +
    "</div>",
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="createApp()">创建</button>');
}

function createApp() {
  api("/admin/apps", {
    method: "POST",
    body: {
      appId: $("appId").value,
      appName: $("appName").value,
      appType: $("appType").value,
      needMobileLogin: $("needMobileLogin").checked,
      allowPasswordLogin: $("allowPasswordLogin").checked,
      allowAvatarUpload: $("allowAvatarUpload").checked,
      accessTokenMinutes: $("accessTokenMinutes").value ? Number($("accessTokenMinutes").value) : 30,
      refreshTokenMinutes: $("refreshTokenMinutes").value ? Number($("refreshTokenMinutes").value) : 43200,
      needDeviceVip: $("needDeviceVip").checked,
      enableUserAiKey: $("enableUserAiKey").checked,
      defaultAiQuotaUnits: $("defaultAiQuotaUnits").value ? Number($("defaultAiQuotaUnits").value) : 0,
      defaultAiProviderCode: $("defaultAiProviderCode").value
    }
  }).then(function (data) {
    closeModal();
    openSecretModal("APP 已创建", data.appId, data.appSecret, data.id);
    renderApps();
  }).catch(function (err) { toast(err.message); });
}

function openAppDetail(id) {
  api("/admin/apps/" + id + "/aggregate").then(function (data) {
    var item = data.app;
    var body = detailList({
      "ID": item.id,
      "APP ID": item.appId,
      "名称": item.appName,
      "类型": item.appType,
      "密钥版本": item.appSecretVersion,
      "手机号登录": item.needMobileLogin,
      "密码登录": item.allowPasswordLogin,
      "头像上传": item.allowAvatarUpload,
      "Access Token 时长": (item.accessTokenMinutes || 30) + " 分钟",
      "Refresh Token 时长": (item.refreshTokenMinutes || 43200) + " 分钟",
      "设备会员": item.needDeviceVip,
      "启用用户AI Key": item.enableUserAiKey,
      "默认AI配额": item.defaultAiQuotaUnits,
      "默认AI平台": item.defaultAiProviderCode,
      "状态": item.status
    }) + sectionBlock("关键指标", statsGrid(data.stats)) +
      sectionBlock("套餐", compactTable([
        { title: "ID", key: "id" }, { title: "名称", key: "packageName" }, { title: "价格(元)", render: function (r) { return formatMoney(r.priceCents); } }, { title: "状态", render: function (r) { return badge(r.status); } }
      ], data.packages)) +
      sectionBlock("最近订单", compactTable([
        { title: "ID", key: "id" }, { title: "订单号", key: "orderNo" }, { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } }, { title: "状态", render: function (r) { return badge(r.payStatus); } }
      ], data.recentOrders)) +
      sectionBlock("最近设备", compactTable([
        { title: "ID", key: "id" }, { title: "设备码", key: "deviceCode" }, { title: "用户", key: "userId" }, { title: "最近启动", key: "lastLaunchAt" }
      ], data.recentDevices));
    openModal("APP 详情", body,
      '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(item.appId || "") + '\')">复制 APP ID</button>' +
      '<button type="button" onclick="openAppEdit(' + item.id + ')">编辑</button>' +
      '<button class="secondary" type="button" onclick="toggleApp(' + item.id + ', \'' + item.status + '\')">启停</button>' +
      '<button class="secondary" type="button" onclick="resetSecret(' + item.id + ')">重置密钥</button>' +
      '<button class="danger" type="button" onclick="deleteApp(' + item.id + ')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openAppEdit(id) {
  api("/admin/apps").then(function (rows) {
    var item = findById(rows, id);
    if (!item) throw new Error("APP 不存在");
    openModal("编辑 APP", '<div class="form-grid">' +
      input("editAppName", "APP 名称", item.appName) +
      checkbox("editNeedMobileLogin", "手机号登录", !!item.needMobileLogin) +
      checkbox("editAllowPasswordLogin", "密码登录", !!item.allowPasswordLogin) +
      checkbox("editAllowAvatarUpload", "头像上传", item.allowAvatarUpload !== false) +
      input("editAccessTokenMinutes", "Access Token 时长(分钟)", item.accessTokenMinutes || 30, { required: false, type: "number" }) +
      input("editRefreshTokenMinutes", "Refresh Token 时长(分钟)", item.refreshTokenMinutes || 43200, { required: false, type: "number" }) +
      checkbox("editNeedDeviceVip", "设备会员", !!item.needDeviceVip) +
      checkbox("editEnableUserAiKey", "启用用户AI Key", !!item.enableUserAiKey) +
      input("editDefaultAiQuotaUnits", "默认AI配额", item.defaultAiQuotaUnits || 0, { required: false, type: "number" }) +
      input("editDefaultAiProviderCode", "默认AI平台", item.defaultAiProviderCode || "api2d", { required: false }) +
      "</div>",
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveAppEdit(' + id + ')">保存</button>');
  }).catch(function (err) { toast(err.message); });
}

function saveAppEdit(id) {
  api("/admin/apps/" + id, {
    method: "PUT",
    body: {
      appName: $("editAppName").value,
      needMobileLogin: $("editNeedMobileLogin").checked,
      allowPasswordLogin: $("editAllowPasswordLogin").checked,
      allowAvatarUpload: $("editAllowAvatarUpload").checked,
      accessTokenMinutes: $("editAccessTokenMinutes").value ? Number($("editAccessTokenMinutes").value) : 30,
      refreshTokenMinutes: $("editRefreshTokenMinutes").value ? Number($("editRefreshTokenMinutes").value) : 43200,
      needDeviceVip: $("editNeedDeviceVip").checked,
      enableUserAiKey: $("editEnableUserAiKey").checked,
      defaultAiQuotaUnits: $("editDefaultAiQuotaUnits").value ? Number($("editDefaultAiQuotaUnits").value) : 0,
      defaultAiProviderCode: $("editDefaultAiProviderCode").value
    }
  }).then(function () {
    toast("APP 已更新");
    closeModal();
    renderApps();
  }).catch(function (err) { toast(err.message); });
}

function toggleApp(id, status) {
  var next = status === "ENABLED" ? "DISABLED" : "ENABLED";
  openRiskConfirm("确认变更 APP 状态", "即将把 APP 状态改为「" + labelOf(next) + "」，可能影响外部项目接入。", "确认变更", function (reason) {
    api("/admin/apps/" + id + "/status", {
      method: "PATCH",
      body: { status: next, confirmReason: reason }
    }).then(function () {
      toast("状态已更新");
      closeModal();
      renderApps();
    }).catch(function (err) { toast(err.message); });
  });
}

function openSecretModal(title, appId, secret, id) {
  openModal(title, detailList({
    "APP": appId,
    "Secret": secret,
    "提示": "该 Secret 只建议在此时复制保存，后续可再次重置生成新值"
  }), '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(secret || '') + '\')">一键复制 Secret</button>' +
    '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
}

function resetSecret(id) {
  openRiskConfirm("确认重置 APP 密钥", "重置后旧密钥会失效，已接入项目需要同步更新配置。", "确认重置", function (reason) {
    api("/admin/apps/" + id + "/reset-secret", { method: "POST", body: { confirmReason: reason } }).then(function (data) {
      closeModal();
      openSecretModal("新 Secret 已生成", data.appId, data.appSecret, id);
      renderApps();
    }).catch(function (err) { toast(err.message); });
  });
}

function deleteApp(id) {
  openRiskConfirm("确认删除 APP", "删除仅允许在 APP 下没有套餐、订单、绑定和设备数据时执行。", "确认删除", function (reason) {
    api("/admin/apps/" + id, { method: "DELETE", body: { confirmReason: reason } }).then(function () {
      toast("APP 已删除");
      closeModal();
      renderApps();
    }).catch(function (err) { toast(err.message); });
  });
}

function paymentChannelOptions(includeAll) {
  var values = includeAll ? [{ value: "", label: "全部渠道" }] : [];
  return values.concat([
    optionOf("ALIPAY"),
    optionOf("WECHAT"),
    optionOf("AGGREGATE")
  ]);
}

function analyticsMetricOptions() {
  var values = [];
  var key;
  for (key in analyticsMetricLabels) {
    values.push({ value: key, label: analyticsMetricLabels[key] });
  }
  return values;
}

function analyticsGranularityOptions() {
  return [
    { value: "DAY", label: analyticsGranularityLabels.DAY },
    { value: "MONTH", label: analyticsGranularityLabels.MONTH },
    { value: "YEAR", label: analyticsGranularityLabels.YEAR }
  ];
}

function paymentConfigStatusOptions(includeAll) {
  var values = includeAll ? [{ value: "", label: "全部状态" }] : [];
  return values.concat([
    optionOf("ENABLED"),
    optionOf("DISABLED")
  ]);
}

function notificationStatusOptions(includeAll) {
  var values = includeAll ? [{ value: "", label: "全部状态" }] : [];
  return values.concat([
    optionOf("ENABLED"),
    optionOf("DISABLED")
  ]);
}

function notificationProviderOptions(type, includeAll) {
  var values = includeAll ? [{ value: "", label: "全部平台" }] : [];
  var providers = type === "SMS" ? [
    { value: "aliyun", label: "阿里云" },
    { value: "tencent", label: "腾讯云" },
    { value: "aggregate", label: "聚合短信" },
    { value: "local", label: "本地日志" }
  ] : [
    { value: "smtp", label: "SMTP 邮箱" },
    { value: "aliyun-dm", label: "阿里云邮件推送" },
    { value: "tencent-ses", label: "腾讯云 SES" },
    { value: "sendcloud", label: "SendCloud" },
    { value: "mailgun", label: "Mailgun" },
    { value: "local", label: "本地日志" }
  ];
  return values.concat(providers);
}

function notificationPrefix(type) {
  return type === "SMS" ? "smsCfg" : "emailCfg";
}

function notificationViewKey(type) {
  return type === "SMS" ? "smsConfigs" : "emailConfigs";
}

function notificationDefault(type, provider) {
  var map = notificationDefaults[type] || {};
  return map[provider] || map.local || {
    displayName: labelOf(provider),
    senderName: "联付中枢",
    senderAddress: "",
    endpoint: "",
    templateCode: "",
    accessKeyId: "",
    accessKeySecret: "",
    secretId: "",
    secretKey: "",
    sdkAppId: "",
    region: "",
    configJson: "{}",
    credentialJson: ""
  };
}

function bindNotificationProviderDefault(type) {
  var prefix = notificationPrefix(type);
  var provider = $(prefix + "CreateProvider");
  if (!provider) return;
  if (type === "SMS") syncSmsProviderFields(prefix + "Create", provider.value);
  syncNotificationExtraFields(type, prefix + "Create", provider.value);
  provider.addEventListener("change", function () {
    var d = notificationDefault(type, provider.value);
    var config = parseJsonObject(d.configJson || "{}");
    var credential = parseJsonObject(d.credentialJson || "{}");
    $(prefix + "CreateName").value = d.displayName || "";
    $(prefix + "CreateSenderName").value = d.senderName || "";
    $(prefix + "CreateSenderAddress").value = d.senderAddress || "";
    $(prefix + "CreateEndpoint").value = d.endpoint || "";
    var template = $(prefix + "CreateTemplateCode");
    if (template) template.value = d.templateCode || "";
    var accessKeyId = $(prefix + "CreateAccessKeyId");
    if (accessKeyId) accessKeyId.value = d.accessKeyId || "";
    var accessKeySecret = $(prefix + "CreateAccessKeySecret");
    if (accessKeySecret) accessKeySecret.value = d.accessKeySecret || "";
    var secretId = $(prefix + "CreateSecretId");
    if (secretId) secretId.value = d.secretId || "";
    var secretKey = $(prefix + "CreateSecretKey");
    if (secretKey) secretKey.value = d.secretKey || "";
    var sdkAppId = $(prefix + "CreateSdkAppId");
    if (sdkAppId) sdkAppId.value = d.sdkAppId || "";
    var region = $(prefix + "CreateRegion");
    if (region) region.value = d.region || "";
    setNotificationExtraDefaults(type, prefix + "Create", provider.value, config, credential);
    if (type === "SMS") syncSmsProviderFields(prefix + "Create", provider.value);
    syncNotificationExtraFields(type, prefix + "Create", provider.value);
  });
}

function renderPaymentConfigs(page) {
  if (typeof page === "number") setPage("paymentConfigs", page);
  page = currentPage("paymentConfigs");
  return api("/admin/payment-configs?page=" + page + "&size=20").then(function (data) {
    var rows = pageContent(data);
    $("paymentConfigs").innerHTML =
      panelTitleActions("支付配置列表",
        '<button type="button" onclick="openPaymentConfigCreate()">新建配置</button>' +
        '<button class="secondary" type="button" onclick="exportPaymentConfigs()">导出</button>') +
      table([
        { title: "ID", key: "id" },
        { title: "渠道", render: function (r) { return formatValue(r.payChannel); } },
        { title: "提供方", key: "providerCode" },
        { title: "商户号", key: "merchantId" },
        { title: "凭据", render: function (r) { return r.credentialConfigured ? "已配置" : "未配置"; } },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openPaymentConfigDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="checkPaymentConfig(' + r.id + ')">检查</button>' +
              '<button class="small" onclick="openPaymentConfigEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="togglePaymentConfig(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '<button class="small danger" onclick="deletePaymentConfig(' + r.id + ')">删除</button>' +
              '</div>';
          }
        }
      ], rows) +
      renderPager("paymentConfigs", pageMeta(data), "renderPaymentConfigs") +
      '</div>';
  });
}

function applyPaymentConfigFilter() {
  renderPaymentConfigs(0);
}

function renderPaymentConfigForm(prefix, item, mode) {
  var normal = parseJsonObject(item.configJson || "{}");
  var credential = parseJsonObject(mode === "edit" ? "{}" : (item.credentialJson || "{}"));
  var channel = item.payChannel || "ALIPAY";
  return '<div class="form-grid payment-config-form">' +
    select(prefix + "Channel", "支付渠道", paymentChannelOptions(false), channel) +
    input(prefix + "Provider", "提供方编码", item.providerCode || providerDefaultFor(channel)) +
    paymentChannelFields(prefix, channel, item, normal, credential, mode) +
    '</div>';
}

function paymentChannelFields(prefix, channel, item, normal, credential, mode) {
  if (channel === "WECHAT") return wechatPaymentFields(prefix, item, normal, credential, mode);
  if (channel === "AGGREGATE") return aggregatePaymentFields(prefix, item, normal, credential, mode);
  return alipayPaymentFields(prefix, item, normal, credential, mode);
}

function alipayPaymentFields(prefix, item, normal, credential, mode) {
  var links = alipayOpenLinks();
  var sandbox = normal.sandbox === true;
  return noteBlock(alipayConfigHint()) +
    inputWithLink(prefix + "Merchant", "支付宝商户 PID", item.merchantId || "", links.pid, "", { required: false, hint: "直连应用通常可不填；仅部分场景需要" }) +
    checkbox(prefix + "Sandbox", "启用沙箱模式", sandbox) +
    select(prefix + "DefaultPayMode", "默认拉起方式", [
      { value: "QR", label: "扫码支付/二维码" },
      { value: "PAGE", label: "浏览器网页支付" },
      { value: "APP", label: "App SDK 支付" }
    ], normal.defaultPayMode || "PAGE") +
    sectionTitle(envTitle(sandbox)) +
    inputWithLink(prefix + "EnvAppId", sandbox ? "沙箱 AppId" : "正式 AppId", envValue(normal, "sandboxAppId", "prodAppId", "channelAppId", sandbox), currentAlipayLink(links, sandbox, 'app'), "", { hint: sandbox ? "沙箱应用 AppId" : "正式环境应用 AppId" }) +
    input(prefix + "EnvGatewayUrl", sandbox ? "沙箱网关" : "正式网关", envValue(normal, "sandboxGatewayUrl", "prodGatewayUrl", "gatewayUrl", sandbox) || alipayGatewayDefault(sandbox), { required: false }) +
    input(prefix + "EnvNotifyUrl", sandbox ? "沙箱异步回调" : "正式异步回调", envValue(normal, "sandboxNotifyUrl", "prodNotifyUrl", "notifyUrl", sandbox) || paymentNotifyTemplate("ALIPAY"), { required: false }) +
    input(prefix + "EnvReturnUrl", sandbox ? "沙箱同步跳转" : "正式同步跳转", envValue(normal, "sandboxReturnUrl", "prodReturnUrl", "returnUrl", sandbox) || paymentReturnTemplate(), { required: false }) +
    input(prefix + "EnvSignType", sandbox ? "沙箱签名方式" : "正式签名方式", envValue(normal, "sandboxSignType", "prodSignType", "signType", sandbox) || "RSA2", { required: false }) +
    textareaWithLink(prefix + "EnvPrivateKey", sandbox ? (mode === "edit" ? "沙箱应用私钥（留空不修改）" : "沙箱应用私钥") : (mode === "edit" ? "正式应用私钥（留空不修改）" : "正式应用私钥"), envCredentialValue(credential, "sandboxMerchantPrivateKey", "prodMerchantPrivateKey", "merchantPrivateKey", sandbox), links.key, "", { required: mode !== "edit" }) +
    textareaWithLink(prefix + "EnvAlipayPublicKey", sandbox ? (mode === "edit" ? "沙箱支付宝公钥（留空不修改）" : "沙箱支付宝公钥") : (mode === "edit" ? "正式支付宝公钥（留空不修改）" : "正式支付宝公钥"), envCredentialValue(credential, "sandboxAlipayPublicKey", "prodAlipayPublicKey", "alipayPublicKey", sandbox), links.key, "", { required: mode !== "edit" });
}

function wechatPaymentFields(prefix, item, normal, credential, mode) {
  return input(prefix + "Merchant", "微信商户号 mchId", item.merchantId || "") +
    input(prefix + "ChannelApp", "微信 AppId", item.channelAppId || "") +
    input(prefix + "Notify", "支付通知地址", item.notifyUrl || paymentNotifyTemplate("WECHAT"), { required: false }) +
    select(prefix + "DefaultPayMode", "默认拉起方式", [
      { value: "QR", label: "Native 扫码支付" },
      { value: "PAGE", label: "H5/浏览器支付" },
      { value: "APP", label: "App 支付" }
    ], normal.defaultPayMode || "QR") +
    input(prefix + "ApiV3Key", mode === "edit" ? "APIv3 密钥（留空不修改）" : "APIv3 密钥", credential.apiV3Key || "", { type: "password", required: mode !== "edit" }) +
    textarea(prefix + "PrivateKey", mode === "edit" ? "商户私钥（留空不修改）" : "商户私钥", credential.merchantPrivateKey || "", { required: mode !== "edit" }) +
    input(prefix + "SerialNo", "商户证书序列号", normal.merchantSerialNo || "", { required: false });
}

function aggregatePaymentFields(prefix, item, normal, credential, mode) {
  return input(prefix + "Merchant", "聚合商户号", item.merchantId || "") +
    input(prefix + "ChannelApp", "渠道应用 ID", item.channelAppId || "", { required: false }) +
    input(prefix + "Notify", "回调地址", item.notifyUrl || paymentNotifyTemplate("AGGREGATE"), { required: false }) +
    input(prefix + "GatewayUrl", "聚合支付网关", normal.gatewayUrl || "") +
    select(prefix + "DefaultPayMode", "默认拉起方式", [
      { value: "QR", label: "二维码/收银台链接" },
      { value: "PAGE", label: "浏览器收银台" },
      { value: "APP", label: "客户端参数" }
    ], normal.defaultPayMode || "PAGE") +
    input(prefix + "ApiKey", mode === "edit" ? "API Key（留空不修改）" : "API Key", credential.apiKey || "", { type: "password", required: mode !== "edit" }) +
    input(prefix + "SignSecret", mode === "edit" ? "签名密钥（留空不修改）" : "签名密钥", credential.signSecret || "", { type: "password", required: mode !== "edit" });
}

function bindPaymentChannelForm(prefix, item, mode) {
  var channel = $(prefix + "Channel");
  var provider = $(prefix + "Provider");
  if (!channel) return;
  function snapshotCurrentAlipayEnv() {
    var sandbox = $(prefix + 'Sandbox');
    if (!sandbox || channel.value !== 'ALIPAY') return;
    var normal = parseJsonObject(item.configJson || '{}');
    var credential = parseJsonObject(item.credentialJson || '{}');
    if (sandbox.checked) {
      normal.sandboxAppId = ($(prefix + 'EnvAppId') || {}).value || normal.sandboxAppId;
      normal.sandboxGatewayUrl = ($(prefix + 'EnvGatewayUrl') || {}).value || '';
      normal.sandboxNotifyUrl = ($(prefix + 'EnvNotifyUrl') || {}).value || '';
      normal.sandboxReturnUrl = ($(prefix + 'EnvReturnUrl') || {}).value || '';
      normal.sandboxSignType = ($(prefix + 'EnvSignType') || {}).value || 'RSA2';
      credential.sandboxMerchantPrivateKey = ($(prefix + 'EnvPrivateKey') || {}).value || '';
      credential.sandboxAlipayPublicKey = ($(prefix + 'EnvAlipayPublicKey') || {}).value || '';
    } else {
      normal.prodAppId = ($(prefix + 'EnvAppId') || {}).value || normal.prodAppId;
      normal.prodGatewayUrl = ($(prefix + 'EnvGatewayUrl') || {}).value || '';
      normal.prodNotifyUrl = ($(prefix + 'EnvNotifyUrl') || {}).value || '';
      normal.prodReturnUrl = ($(prefix + 'EnvReturnUrl') || {}).value || '';
      normal.prodSignType = ($(prefix + 'EnvSignType') || {}).value || 'RSA2';
      credential.prodMerchantPrivateKey = ($(prefix + 'EnvPrivateKey') || {}).value || '';
      credential.prodAlipayPublicKey = ($(prefix + 'EnvAlipayPublicKey') || {}).value || '';
    }
    normal.sandbox = sandbox.checked;
    item.configJson = JSON.stringify(normal);
    item.credentialJson = JSON.stringify(credential);
  }
  function rerender() {
    snapshotCurrentAlipayEnv();
    item.payChannel = channel.value;
    item.providerCode = providerDefaultFor(channel.value);
    openModal(mode === "create" ? "新建支付配置" : "编辑支付配置", renderPaymentConfigForm(prefix, item, mode),
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      (mode === "create" ? '<button type="button" onclick="createPaymentConfig()">创建</button>' : '<button type="button" onclick="savePaymentConfigEdit(' + item.id + ')">保存</button>'));
    bindPaymentChannelForm(prefix, item, mode);
    var ch = $(prefix + 'Channel'); if (mode === 'edit' && ch) ch.disabled = true;
  }
  if (mode === "create") channel.addEventListener("change", rerender);
  var sandbox = $(prefix + 'Sandbox');
  if (sandbox && channel.value === 'ALIPAY') sandbox.addEventListener('change', rerender);
  if (provider) provider.value = item.providerCode || providerDefaultFor(channel.value);
}

function openPaymentConfigCreate() {
  var item = {
    appId: "__PLATFORM__",
    payChannel: "ALIPAY",
    providerCode: providerDefaultFor("ALIPAY"),
    merchantId: "",
    channelAppId: "",
    notifyUrl: "",
    configJson: "{}",
    credentialJson: ""
  };
  openModal("新建支付配置", renderPaymentConfigForm("payCfgCreate", item, "create"),
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="createPaymentConfig()">创建</button>');
  bindPaymentChannelForm("payCfgCreate", item, "create");
}

function createPaymentConfig() {
  api("/admin/payment-configs", {
    method: "POST",
    body: Object.assign({
      appId: "__PLATFORM__",
      payChannel: $("payCfgCreateChannel").value
    }, paymentConfigBody("payCfgCreate", true))
  }).then(function () {
    toast("支付配置已创建");
    closeModal();
    renderPaymentConfigs(0);
  }).catch(function (err) { toast(err.message); });
}

function openPaymentConfigDetail(id) {
  api("/admin/payment-configs/" + id).then(function (item) {
    openModal("支付配置详情", detailList({
      "ID": item.id,
      "渠道": item.payChannel,
      "提供方": item.providerCode,
      "商户号": item.merchantId,
      "渠道 APP ID": item.channelAppId,
      "回调地址": item.notifyUrl,
      "建议回调路径": paymentNotifyTemplate(item.payChannel),
      "敏感凭据": item.credentialConfigured ? "已配置" : "未配置",
      "状态": item.status,
      "创建时间": item.createdAt,
      "更新时间": item.updatedAt
    }), '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(paymentNotifyTemplate(item.payChannel)) + '\')">复制回调路径</button>' +
      '<button class="danger" type="button" onclick="deletePaymentConfig(' + id + ')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function checkPaymentConfig(id) {
  api("/admin/payment-configs/" + id + "/check").then(function (data) {
    var warnings = data.warnings && data.warnings.length ? data.warnings.join("\n") : "配置检查通过";
    openModal("支付配置检查", detailList({
      "是否就绪": data.ready ? "是" : "否",
      "建议回调路径": data.suggestedNotifyPath,
      "检查结果": warnings
    }), '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(data.suggestedNotifyPath || "") + '\')">复制回调路径</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openPaymentConfigEdit(id) {
  api("/admin/payment-configs/" + id).then(function (item) {
    openModal("编辑支付配置", renderPaymentConfigForm("payCfgEdit", item, "edit"),
      '<button class="danger" type="button" onclick="deletePaymentConfig(' + id + ')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="savePaymentConfigEdit(' + id + ')">保存</button>');
    bindPaymentChannelForm("payCfgEdit", item, "edit");
    var channel = $("payCfgEditChannel");
    if (channel) channel.disabled = true;
  }).catch(function (err) { toast(err.message); });
}

function savePaymentConfigEdit(id) {
  api("/admin/payment-configs/" + id, {
    method: "PUT",
    body: paymentConfigBody("payCfgEdit", false)
  }).then(function () {
    toast("支付配置已更新");
    closeModal();
    renderPaymentConfigs(currentPage("paymentConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function togglePaymentConfig(id, status) {
  api("/admin/payment-configs/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("支付配置状态已更新");
    renderPaymentConfigs(currentPage("paymentConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function deletePaymentConfig(id) {
  if (!confirm("确定删除这个支付配置？")) return;
  api("/admin/payment-configs/" + id, { method: "DELETE" }).then(function () {
    toast("支付配置已删除");
    closeModal();
    renderPaymentConfigs(currentPage("paymentConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function renderSmsConfigs(page) {
  return renderNotificationConfigs("SMS", page);
}

function renderEmailConfigs(page) {
  return renderNotificationConfigs("EMAIL", page);
}

function renderNotificationConfigs(type, page) {
  var viewKey = notificationViewKey(type);
  if (typeof page === "number") setPage(viewKey, page);
  page = currentPage(viewKey);
  return api("/admin/notification-configs?channelType=" + type + "&page=" + page + "&size=20").then(function (data) {
    var rows = pageContent(data);
    if (type === "SMS") {
      setFilters("smsSendLogs", queryFilters("smsSendLogs"));
      return api("/admin/notification-configs/sms/logs?page=" + currentPage("smsSendLogs") + "&size=10").then(function (logData) {
        $(viewKey).innerHTML =
          panelTitleActions("短信配置列表",
            '<button type="button" onclick="openNotificationConfigCreate(\'SMS\')">新建配置</button>' +
            '<button class="secondary" type="button" onclick="exportNotificationConfigs(\'SMS\')">导出</button>') +
          table([
            { title: "ID", key: "id" },
            { title: "平台", render: function (r) { return formatValue(r.providerCode); } },
            { title: "名称", key: "displayName" },
            { title: "签名", key: "senderName" },
            { title: "地址", key: "senderAddress" },
            { title: "服务地址", key: "endpoint" },
            { title: "凭据", render: function (r) { return r.credentialConfigured ? "已配置" : "未配置"; } },
            { title: "状态", render: function (r) { return badge(r.status); } },
            {
              title: "操作",
              render: function (r) {
                return '<div class="actions">' +
                  '<button class="small" onclick="openNotificationConfigDetail(' + r.id + ')">详情</button>' +
                  '<button class="small" onclick="openNotificationConfigEdit(' + r.id + ')">编辑</button>' +
                  '<button class="small" onclick="toggleNotificationConfig(' + r.id + ', \'' + r.status + '\')">启停</button>' +
                  '<button class="small danger" onclick="deleteNotificationConfig(' + r.id + ', \'' + type + '\')">删除</button>' +
                  '</div>';
              }
            }
          ], rows) +
          renderPager(viewKey, pageMeta(data), "renderSmsConfigs") +
          '<div style="height:12px"></div>' +
          panel("最近短信发送记录", table([
            { title: "ID", key: "id" },
            { title: "平台", render: function (r) { return formatValue(r.providerCode); } },
            { title: "通道ID", key: "configId" },
            { title: "APP ID", key: "appId" },
            { title: "手机号", key: "mobile" },
            { title: "模板编码", key: "templateCode" },
            { title: "消息ID", key: "messageId" },
            { title: "结果", render: function (r) { return r.success ? badge("SUCCESS") : badge("FAILED"); } },
            { title: "说明", key: "resultMessage" },
            { title: "时间", key: "createdAt" }
          ], pageContent(logData)) + renderPager("smsSendLogs", pageMeta(logData), "renderSmsSendLogs"));
      });
    }
    $(viewKey).innerHTML =
      panelTitleActions("邮件配置列表",
        '<button type="button" onclick="openNotificationConfigCreate(\'EMAIL\')">新建配置</button>' +
        '<button class="secondary" type="button" onclick="openEmailSendModal()">发送测试邮件</button>' +
        '<button class="secondary" type="button" onclick="exportNotificationConfigs(\'EMAIL\')">导出</button>') +
      table([
        { title: "ID", key: "id" },
        { title: "平台", render: function (r) { return formatValue(r.providerCode); } },
        { title: "名称", key: "displayName" },
        { title: "发件人", key: "senderName" },
        { title: "地址", key: "senderAddress" },
        { title: "服务地址", key: "endpoint" },
        { title: "凭据", render: function (r) { return r.credentialConfigured ? "已配置" : "未配置"; } },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openNotificationConfigDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="openNotificationConfigEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="toggleNotificationConfig(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '<button class="small danger" onclick="deleteNotificationConfig(' + r.id + ', \'' + type + '\')">删除</button>' +
              '</div>';
          }
        }
      ], rows) +
      renderPager(viewKey, pageMeta(data), "renderEmailConfigs") +
      '</div>';
  });
}

function renderSmsSendLogs(page) {
  if (typeof page === "number") setPage("smsSendLogs", page);
  return renderSmsConfigs(currentPage("smsConfigs"));
}

function panelTitleActions(title, actions) {
  return '<div class="panel"><div class="panel-title panel-title-split"><div>' + title + '</div><div class="panel-title-actions">' + actions + '</div></div>';
}

function renderNotificationConfigForm(type, prefix, item, mode) {
  var config = parseJsonObject(item.configJson || "{}");
  var credential = parseJsonObject(mode === "edit" ? "{}" : (item.credentialJson || "{}"));
  return '<div class="form-grid notification-config-form">' +
    select(prefix + "Provider", "平台", notificationProviderOptions(type, false), item.providerCode) +
    input(prefix + "Name", "配置名称", item.displayName) +
    input(prefix + "SenderName", type === "SMS" ? "短信签名" : "发件名称", item.senderName) +
    input(prefix + "SenderAddress", type === "SMS" ? "发送签名/扩展码" : "发件邮箱", item.senderAddress, { required: false }) +
    input(prefix + "Endpoint", "服务地址", item.endpoint, { required: false }) +
    (type === "SMS" ? renderSmsConfigFields(prefix, item, item.providerCode, mode) + renderSmsExtraFields(prefix, item.providerCode, config, credential, mode) : renderEmailExtraFields(prefix, item.providerCode, config, credential, mode)) +
    '</div>';
}

function openNotificationConfigCreate(type) {
  var prefix = notificationPrefix(type) + "Create";
  var item = Object.assign({ providerCode: type === "SMS" ? "aliyun" : "smtp" }, notificationDefault(type, type === "SMS" ? "aliyun" : "smtp"));
  openModal(type === "SMS" ? "新建短信配置" : "新建邮件配置", renderNotificationConfigForm(type, prefix, item, "create"),
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="createNotificationConfig(\'' + type + '\')">创建</button>');
  bindNotificationProviderDefault(type);
}

function openSmsSendCodeModal(configId) {
  api("/admin/notification-configs?channelType=SMS&page=0&size=100").then(function (data) {
    var rows = pageContent(data);
    var current = null;
    if (configId) {
      current = rows.find(function (item) { return Number(item.id) === Number(configId); }) || null;
    }
    openModal("发送测试短信", '<div class="form-grid send-form">' +
      input("smsSendAppId", "APP ID", current && current.appId ? current.appId : "", { required: false }) +
      input("smsSendMobile", "手机号", "13800000000") +
      input("smsSendCode", "验证码", "123456") +
      checkbox("smsSendReal", "真实发送（默认关闭，关闭时走本地日志通道）", false) +
      '</div>',
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="sendSmsCodeMessage(' + (configId ? Number(configId) : 'null') + ')">发送</button>');
  }).catch(function (err) { toast(err.message); });
}

function sendSmsCodeMessage(configId) {
  api("/admin/notification-configs/sms/send-code", {
    method: "POST",
    body: {
      configId: configId ? Number(configId) : null,
      appId: $("smsSendAppId").value,
      mobile: $("smsSendMobile").value,
      code: $("smsSendCode").value,
      realSend: $("smsSendReal").checked
    }
  }).then(function (data) {
    toast(data.message || ($("smsSendReal").checked ? "短信已发送" : "测试短信已写入本地日志"));
    closeModal();
    renderSmsConfigs(currentPage("smsConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function createNotificationConfig(type) {
  var prefix = notificationPrefix(type) + "Create";
  var body = notificationBody(type, prefix);
  body.channelType = type;
  api("/admin/notification-configs", {
    method: "POST",
    body: body
  }).then(function () {
    toast(type === "SMS" ? "短信配置已创建" : "邮件配置已创建");
    closeModal();
    if (type === "SMS") renderSmsConfigs(0); else renderEmailConfigs(0);
  }).catch(function (err) { toast(err.message); });
}

function renderEmailSendPanel(rows) {
  var options = [{ value: "", label: "自动选择启用通道" }].concat(rows.map(function (item) {
    return { value: String(item.id), label: labelOf(item.providerCode) + " / " + item.displayName };
  }));
  return '<div class="form-grid send-form">' +
    select("emailSendConfigId", "通道", options, "", { required: false }) +
    input("emailSendTo", "收件邮箱", "user@example.com") +
    input("emailSendSubject", "主题", "联付中枢测试邮件") +
    checkbox("emailSendHtml", "HTML 邮件", false) +
    textarea("emailSendContent", "邮件内容", "这是一封来自联付中枢的测试邮件。") +
    '<div class="form-actions"><button type="button" onclick="sendEmailMessage()">发送邮件</button></div>' +
    '</div>';
}

function applyNotificationConfigFilter(type) {
  var viewKey = notificationViewKey(type);
  var prefix = notificationPrefix(type);
  setFilters(viewKey, {
    providerCode: $(prefix + "ProviderFilter").value,
    status: $(prefix + "StatusFilter").value
  });
  if (type === "SMS") renderSmsConfigs(0); else renderEmailConfigs(0);
}

function openNotificationConfigDetail(id) {
  api("/admin/notification-configs/" + id).then(function (item) {
    openModal("通知配置详情", detailList(notificationDetailFields(item)),
      '<button class="danger" type="button" onclick="deleteNotificationConfig(' + id + ', \'' + item.channelType + '\')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openNotificationConfigEdit(id) {
  api("/admin/notification-configs/" + id).then(function (item) {
    var footer = '';
    if (item.channelType === "SMS") {
      footer += '<button class="secondary" type="button" onclick="openSmsSendCodeModal(' + id + ')">测试</button>';
    }
    footer += '<button class="danger" type="button" onclick="deleteNotificationConfig(' + id + ', \'' + item.channelType + '\')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveNotificationConfigEdit(' + id + ', \'' + item.channelType + '\')">保存</button>';
    openModal("编辑通知配置", renderNotificationEditFields(item), footer);
    bindNotificationEditProvider(item);
  }).catch(function (err) { toast(err.message); });
}

function saveNotificationConfigEdit(id, type) {
  api("/admin/notification-configs/" + id, {
    method: "PUT",
    body: notificationBody(type, "notifyEdit")
  }).then(function () {
    toast("通知配置已更新");
    closeModal();
    if (type === "SMS") renderSmsConfigs(currentPage("smsConfigs")); else renderEmailConfigs(currentPage("emailConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function toggleNotificationConfig(id, status) {
  api("/admin/notification-configs/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("通知配置状态已更新");
    renderCurrent();
  }).catch(function (err) { toast(err.message); });
}

function deleteNotificationConfig(id, type) {
  if (!confirm("确定删除这个通知配置？")) return;
  api("/admin/notification-configs/" + id, { method: "DELETE" }).then(function () {
    toast("通知配置已删除");
    closeModal();
    if (type === "SMS") renderSmsConfigs(currentPage("smsConfigs")); else renderEmailConfigs(currentPage("emailConfigs"));
  }).catch(function (err) { toast(err.message); });
}

function openEmailSendModal() {
  api("/admin/notification-configs?channelType=EMAIL&page=0&size=100").then(function (data) {
    var rows = pageContent(data);
    openModal("发送测试邮件", renderEmailSendPanel(rows),
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="sendEmailMessage()">发送</button>');
  }).catch(function (err) { toast(err.message); });
}

function sendEmailMessage() {
  api("/admin/notification-configs/email/send", {
    method: "POST",
    body: {
      configId: $("emailSendConfigId").value ? Number($("emailSendConfigId").value) : null,
      to: $("emailSendTo").value,
      subject: $("emailSendSubject").value,
      content: $("emailSendContent").value,
      html: $("emailSendHtml").checked
    }
  }).then(function (data) {
    toast(data.message || "邮件已发送");
    closeModal();
  }).catch(function (err) { toast(err.message); });
}

function renderProducts() {
  return loadApps().then(function (apps) {
    var current = queryFilters("products").appId || (apps[0] && apps[0].appId) || "";
    setFilters("products", { appId: current });
    return api("/admin/products?appId=" + encodeURIComponent(current)).then(function (rows) {
      $("products").innerHTML = panelTitleActions("商品列表",
        '<button type="button" onclick="openProductCreate()">新建商品</button>') +
        table([
          { title: "APP", key: "appId" },
          { title: "商品编码", key: "productCode" },
          { title: "商品名", key: "productName" },
          { title: "类型", key: "productType" },
          { title: "履约", key: "fulfillmentType" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "操作", render: function (r) { return '<div class="actions"><button class="small" onclick="openProductDetail(' + r.id + ')">详情</button><button class="small" onclick="openPlanCreate(' + r.id + ')">加方案</button></div>'; } }
        ], rows || []) + '</div>';
    });
  });
}

function openProductCreate() {
  loadApps().then(function (apps) {
    openModal("新建商品", '<div class="form-grid">' +
      select("productAppId", "APP", apps.map(function (a) { return { value: a.appId, label: a.appId + " / " + a.appName }; }), apps[0] && apps[0].appId || "") +
      input("productCode", "商品编码", "vip") +
      input("productName", "商品名", "会员") +
      select("productType", "商品类型", [optionOf("VIP"), optionOf("AI_CREDITS"), optionOf("FEATURE"), optionOf("OTHER")], "VIP") +
      select("fulfillmentType", "履约类型", [optionOf("MEMBERSHIP_DURATION"), optionOf("CREDIT_GRANT"), optionOf("MANUAL")], "MEMBERSHIP_DURATION") +
      textarea("productDesc", "描述", "", { required: false }) +
      textarea("productBenefits", "权益说明", "", { required: false }) +
      input("productSort", "排序", "0", "number") +
      '</div>', '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveProductCreate()">创建</button>');
  });
}

function saveProductCreate() {
  api("/admin/products", { method: "POST", body: {
    appId: $("productAppId").value,
    productCode: $("productCode").value,
    productName: $("productName").value,
    productType: $("productType").value,
    fulfillmentType: $("fulfillmentType").value,
    description: $("productDesc").value,
    benefitsText: $("productBenefits").value,
    sortOrder: Number($("productSort").value || 0)
  }}).then(function () { closeModal(); renderProducts(); }).catch(function (err) { toast(err.message); });
}

function openProductPreview(encoded) {
  var detail = JSON.parse(decodeURIComponent(String(encoded || "{}")));
  var product = detail.product || {};
  var slug = encodeURIComponent(product.productCode || "preview");
  var previewUrl = "/purchase-ui/index.html?preview=1&name=" + encodeURIComponent(product.productName || "") +
    "&desc=" + encodeURIComponent(product.description || "") +
    "&benefits=" + encodeURIComponent(product.benefitsText || "") +
    "&plans=" + encodeURIComponent(JSON.stringify(detail.plans || [])) +
    "#" + slug;
  var win = window.open(previewUrl, "_blank", "noopener");
  if (!win) toast("浏览器拦截了预览窗口");
}

function openProductDetail(id) {
  api("/admin/products/" + id).then(function (data) {
    var product = data.product || {};
    openModal("编辑商品", '<div class="form-grid">' +
      input("editProductCode", "商品编码", product.productCode || "", { required: false, hint: "编码当前只读展示" }) +
      input("editProductName", "商品名", product.productName || "") +
      input("editProductType", "商品类型", labelOf(product.productType), { required: false }) +
      input("editFulfillmentType", "履约类型", labelOf(product.fulfillmentType), { required: false }) +
      textarea("editProductDesc", "描述", product.description || "", { required: false }) +
      textarea("editProductBenefits", "权益", product.benefitsText || "", { required: false }) +
      input("editProductSort", "排序", product.sortOrder || 0, "number") +
      '</div>' + sectionBlock("方案", compactTable([
        { title: "方案编码", key: "planCode" },
        { title: "名称", key: "planName" },
        { title: "价格(元)", render: function (r) { return formatMoney(r.priceCents); } },
        { title: "时长", key: "durationDays" },
        { title: "算力", key: "creditAmount" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        { title: "操作", render: function (r) { return '<div class="actions"><button class="small" onclick="openPlanEdit(' + r.id + ',\'' + encodeURIComponent(JSON.stringify(r)).replace(/'/g, "%27") + '\',' + product.id + ')">编辑</button></div>'; } }
      ], data.plans || [])), '<button class="secondary" type="button" onclick="openProductPreview(\'' + encodeURIComponent(JSON.stringify(data)).replace(/'/g, "%27") + '\')">预览</button><button class="secondary" type="button" onclick="openPlanCreate(' + product.id + ')">新增方案</button><button type="button" onclick="saveProductEdit(' + product.id + ')">保存商品</button><button class="secondary" type="button" onclick="closeModal()">关闭</button>');
    var code = $("editProductCode"); if (code) code.disabled = true;
    var type = $("editProductType"); if (type) type.disabled = true;
    var fulfill = $("editFulfillmentType"); if (fulfill) fulfill.disabled = true;
  }).catch(function (err) { toast(err.message); });
}

function saveProductEdit(productId) {
  api("/admin/products/" + productId, { method: "PUT", body: {
    productName: $("editProductName").value,
    description: $("editProductDesc").value,
    benefitsText: $("editProductBenefits").value,
    sortOrder: Number($("editProductSort").value || 0)
  }}).then(function () { toast("商品已更新"); openProductDetail(productId); }).catch(function (err) { toast(err.message); });
}

function openPlanCreate(productId) {
  openSubModal("新建方案", '<div class="form-grid">' +
    input("planCode", "方案编码", "vip_month") +
    input("planName", "方案名", "月度会员") +
    input("planPrice", "价格(元)", "9.90", { required: true, type: "number", hint: "单位：元" }) +
    input("planOriginPrice", "原价(元)", "12.90", { required: false, type: "number", hint: "单位：元" }) +
    input("planDays", "时长(天)", "30", { required: false, type: "number" }) +
    input("planCredits", "算力量", "", { required: false, type: "number" }) +
    input("planBadge", "标签", "热门", { required: false }) +
    textarea("planBenefits", "方案权益", "", { required: false }) +
    input("planSort", "排序", "0", "number") +
    '</div>', '<button class="secondary" type="button" onclick="closeSubModal()">取消</button><button type="button" onclick="savePlanCreate(' + productId + ')">创建</button>');
}

function savePlanCreate(productId) {
  api("/admin/products/" + productId + "/plans", { method: "POST", body: {
    planCode: $("planCode").value,
    planName: $("planName").value,
    priceCents: centsFromYuanInput($("planPrice").value) || 0,
    originalPriceCents: centsFromYuanInput($("planOriginPrice").value),
    durationDays: $("planDays").value ? Number($("planDays").value) : null,
    creditAmount: $("planCredits").value ? Number($("planCredits").value) : null,
    badgeText: $("planBadge").value,
    benefitsText: $("planBenefits").value,
    sortOrder: Number($("planSort").value || 0)
  }}).then(function () { closeSubModalToProduct(productId); }).catch(function (err) { toast(err.message); });
}

function savePlanEdit(planId, productId) {
  api("/admin/products/plans/" + planId, { method: "PUT", body: {
    planName: $("editPlanName").value,
    priceCents: centsFromYuanInput($("editPlanPrice").value) || 0,
    originalPriceCents: centsFromYuanInput($("editPlanOriginPrice").value),
    durationDays: $("editPlanDays").value ? Number($("editPlanDays").value) : null,
    creditAmount: $("editPlanCredits").value ? Number($("editPlanCredits").value) : null,
    badgeText: $("editPlanBadge").value,
    benefitsText: $("editPlanBenefits").value,
    sortOrder: Number($("editPlanSort").value || 0)
  }}).then(function () { closeSubModalToProduct(productId); }).catch(function (err) { toast(err.message); });
}

function renderPurchasePages() {
  return loadApps().then(function (apps) {
    var current = queryFilters("purchasePages").appId || (apps[0] && apps[0].appId) || "";
    setFilters("purchasePages", { appId: current });
    return api("/admin/purchase-pages?appId=" + encodeURIComponent(current)).then(function (rows) {
      $("purchasePages").innerHTML = panelTitleActions("购买页列表", '<button type="button" onclick="openPurchasePageCreate()">新建购买页</button>') +
        table([
          { title: "APP", key: "appId" },
          { title: "Slug", key: "pageSlug" },
          { title: "标题", key: "title" },
          { title: "布局", key: "layoutType" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "操作", render: function (r) { return '<div class="actions"><button class="small" onclick="openPurchasePageDetail(' + r.id + ')">详情</button><button class="small" onclick="window.open(\'/p/' + r.pageSlug + '\', \'_blank\')">预览</button></div>'; } }
        ], rows || []) + '</div>';
    });
  });
}

function purchasePageForm(item, appOptions) {
  item = item || {};
  return '<div class="form-grid">' +
    select("pageAppId", "APP", appOptions, item.appId || (appOptions[0] && appOptions[0].value) || "") +
    input("pageSlug", "页面标识", item.pageSlug || "vip") +
    input("pageTitleInput", "标题", item.title || "开通会员") +
    input("pageSubtitle", "副标题", item.subtitle || "选择最适合你的方案", { required: false }) +
    select("pageLayout", "布局", [optionOf("CARD_GRID"), optionOf("SPLIT_HERO"), optionOf("COMPACT")], item.layoutType || "CARD_GRID") +
    input("pageDefaultProductCode", "默认商品编码", item.defaultProductCode || "vip", { required: false }) +
    input("pageDefaultPlanCode", "默认方案编码", item.defaultPlanCode || "", { required: false }) +
    select("pageDefaultPayChannel", "默认支付方式", [optionOf("ALIPAY"), optionOf("WECHAT"), optionOf("AGGREGATE")], item.defaultPayChannel || "ALIPAY") +
    textarea("pageTheme", "主题 JSON", item.themeJson || '{"primaryColor":"#4f46e5"}', { required: false }) +
    textarea("pageContentJson", "内容 JSON", item.contentJson || '{"products":["vip"]}', { required: false }) +
    '</div>';
}

function purchasePageBody() {
  return {
    appId: $("pageAppId").value,
    pageSlug: $("pageSlug").value,
    title: $("pageTitleInput").value,
    subtitle: $("pageSubtitle").value,
    layoutType: $("pageLayout").value,
    themeJson: $("pageTheme").value,
    contentJson: $("pageContentJson").value,
    defaultProductCode: $("pageDefaultProductCode").value,
    defaultPlanCode: $("pageDefaultPlanCode").value,
    defaultPayChannel: $("pageDefaultPayChannel").value
  };
}

function openPurchasePageCreate() {
  loadApps().then(function (apps) {
    var appOptions = apps.map(function (a) { return { value: a.appId, label: a.appId + " / " + a.appName }; });
    openModal("新建购买页", purchasePageForm({}, appOptions), '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="savePurchasePageCreate()">创建</button>');
  });
}

function savePurchasePageCreate() {
  api("/admin/purchase-pages", { method: "POST", body: purchasePageBody() }).then(function () { closeModal(); renderPurchasePages(); }).catch(function (err) { toast(err.message); });
}

function openPurchasePageDetail(id) {
  api("/admin/purchase-pages/" + id).then(function (item) {
    var appOptions = [{ value: item.appId, label: item.appId + " / 当前 APP" }];
    openModal("编辑购买页", purchasePageForm(item, appOptions), '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(location.origin + '/p/' + item.pageSlug) + '\')">复制预览地址</button><button class="secondary" type="button" onclick="window.open(\'/p/' + item.pageSlug + '\', \'_blank\')">打开预览</button><button type="button" onclick="savePurchasePageEdit(' + item.id + ')">保存</button><button class="secondary" type="button" onclick="closeModal()">关闭</button>');
    var app = $("pageAppId"); if (app) app.disabled = true;
    var slug = $("pageSlug"); if (slug) slug.disabled = true;
  }).catch(function (err) { toast(err.message); });
}

function savePurchasePageEdit(id) {
  api("/admin/purchase-pages/" + id, { method: "PUT", body: purchasePageBody() }).then(function () { toast("购买页已更新"); openPurchasePageDetail(id); }).catch(function (err) { toast(err.message); });
}

function renderPackages() {
  return loadApps().then(function (apps) {
    var current = queryFilters("packages").appId || (apps[0] && apps[0].appId) || "";
    setFilters("packages", { appId: current });
    return loadPackagesByApp(current).then(function (rows) {
      var filterBar = '<div class="toolbar">' +
        select("pkgAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), current) +
        '<button class="secondary" type="button" onclick="applyPackageFilter()">筛选</button>' +
        '<button type="button" onclick="openPackageCreate()">创建套餐</button>' +
        '<button class="secondary" type="button" onclick="exportPackages()">导出</button>' +
        "</div>";

      $("packages").innerHTML = panel("筛选与操作", filterBar) + '<div style="height:12px"></div>' +
        panel("套餐列表", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "名称", key: "packageName" },
          { title: "类型", key: "packageType" },
          { title: "价格(元)", render: function (r) { return formatMoney(r.priceCents); } },
          { title: "天数", key: "durationDays" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openPackageEdit(' + r.id + ')">编辑</button>' +
                '<button class="small" onclick="togglePackage(' + r.id + ', \'' + r.status + '\')">启停</button>' +
                '</div>';
            }
          }
        ], rows));
    });
  });
}

function applyPackageFilter() {
  setFilters("packages", { appId: $("pkgAppFilter").value });
  renderPackages();
}

function loadPackagesByApp(appId) {
  if (!appId) return Promise.resolve([]);
  return api("/admin/packages?appId=" + encodeURIComponent(appId)).then(function (rows) {
    return rows || [];
  });
}

function openPackageCreate() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    openModal("创建套餐", '<div class="form-grid">' +
      select("createPkgAppId", "APP", apps.map(function (a) {
        return { value: a.appId, label: a.appId + " / " + a.appName };
      }), queryFilters("packages").appId || apps[0].appId) +
      input("createPkgName", "套餐名", "月度会员") +
      select("createPkgType", "类型", [
        optionOf("MEMBERSHIP"),
        optionOf("FEATURE")
      ], "MEMBERSHIP") +
      input("createPkgPrice", "价格(元)", "9.90", { required: true, type: "number", hint: "单位：元" }) +
      input("createPkgDays", "天数", "30") +
      textarea("createPkgBenefits", "权益说明", "VIP 权益") +
      "</div>",
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="savePackageCreate()">创建</button>');
  }).catch(function (err) { toast(err.message); });
}

function savePackageCreate() {
  api("/admin/packages", {
    method: "POST",
    body: {
      appId: $("createPkgAppId").value,
      packageName: $("createPkgName").value,
      packageType: $("createPkgType").value,
      priceCents: centsFromYuanInput($("createPkgPrice").value) || 0,
      durationDays: Number($("createPkgDays").value),
      benefitsText: $("createPkgBenefits").value
    }
  }).then(function () {
    toast("套餐已创建");
    closeModal();
    renderPackages();
  }).catch(function (err) { toast(err.message); });
}

function openPackageEdit(id) {
  api("/admin/packages?appId=" + encodeURIComponent(queryFilters("packages").appId || "")).then(function (rows) {
    var item = findById(rows, id);
    if (!item) throw new Error("套餐不存在");
    openModal("编辑套餐", '<div class="form-grid">' +
      input("editPkgName", "套餐名", item.packageName) +
      input("editPkgPrice", "价格(元)", yuanInputFromCents(item.priceCents), { required: true, type: "number", hint: "单位：元" }) +
      input("editPkgDays", "天数", item.durationDays) +
      textarea("editPkgBenefits", "权益说明", item.benefitsText) +
      "</div>",
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="savePackageEdit(' + id + ')">保存</button>');
  }).catch(function (err) { toast(err.message); });
}

function savePackageEdit(id) {
  api("/admin/packages/" + id, {
    method: "PUT",
    body: {
      packageName: $("editPkgName").value,
      priceCents: centsFromYuanInput($("editPkgPrice").value) || 0,
      durationDays: Number($("editPkgDays").value),
      benefitsText: $("editPkgBenefits").value
    }
  }).then(function () {
    toast("套餐已更新");
    closeModal();
    renderPackages();
  }).catch(function (err) { toast(err.message); });
}

function togglePackage(id, status) {
  api("/admin/packages/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("状态已更新");
    renderPackages();
  }).catch(function (err) { toast(err.message); });
}

function renderUsers(page) {
  if (typeof page === "number") setPage("users", page);
  page = currentPage("users");
  var filters = queryFilters("users");
  setFilters("users", { keyword: filters.keyword || "", mobile: filters.mobile || "", username: filters.username || "" });
  var qs = ["page=" + page, "size=20"];
  if (filters.keyword) qs.push("keyword=" + encodeURIComponent(filters.keyword));
  if (filters.mobile) qs.push("mobile=" + encodeURIComponent(filters.mobile));
  if (filters.username) qs.push("username=" + encodeURIComponent(filters.username));
  return api("/admin/users?" + qs.join("&")).then(function (data) {
    var rows = pageContent(data);
    var filterBar = '<div class="toolbar">' +
      input("userKeywordFilter", "关键词", filters.keyword || "") +
      input("userMobileFilter", "手机号", filters.mobile || "") +
      input("userUsernameFilter", "用户名", filters.username || "") +
      '<button class="secondary" type="button" onclick="applyUserFilter()">筛选</button>' +
      '<button class="secondary" type="button" onclick="exportUsers()">导出</button>' +
      "</div>";
    $("users").innerHTML =
      panel("筛选", filterBar) + '<div style="height:12px"></div>' +
      panel("用户列表", table([
        { title: "ID", key: "id" },
        { title: "手机号", key: "mobile" },
        { title: "用户名", render: function (r) { return r.username || "-"; } },
        { title: "昵称", render: function (r) { return r.nickname || "-"; } },
        { title: "头像", render: function (r) { return r.avatarUrl ? '<a href="' + escapeHtml(r.avatarUrl) + '" target="_blank">查看</a>' : "-"; } },
        { title: "类型", key: "userType" },
        { title: "最近登录", render: function (r) { return r.lastLoginAt || "-"; } },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openUserDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="openUserEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="openUserPasswordReset(' + r.id + ')">重置密码</button>' +
              '<button class="small" onclick="toggleUser(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '</div>';
          }
        }
      ], rows)) +
      renderPager("users", pageMeta(data), "renderUsers");
  });
}

function applyUserFilter() {
  setFilters("users", {
    keyword: $("userKeywordFilter").value,
    mobile: $("userMobileFilter").value,
    username: $("userUsernameFilter").value
  });
  renderUsers(0);
}

function openUserDetail(id) {
  Promise.all([api("/admin/users/" + id + "/profile"), api("/admin/user-ai/by-user/" + id)]).then(function (res) {
    var profile = res[0];
    var aiKeys = res[1] || [];
    var user = profile.user || {};
    var stats = profile.stats || {};
    openModal("用户画像",
      detailList({
        "ID": user.id,
        "手机号": user.mobile,
        "用户名": user.username || "-",
        "昵称": user.nickname || "-",
        "头像": user.avatarUrl ? '<a href="' + escapeHtml(user.avatarUrl) + '" target="_blank">查看</a>' : "-",
        "类型": user.userType,
        "状态": user.status,
        "最近登录": user.lastLoginAt || "-",
        "密码设置时间": user.passwordSetAt || "-",
        "需改密": user.mustChangePassword ? "是" : "否",
        "AI Key 数": aiKeys.length,
        "创建时间": user.createdAt,
        "更新时间": user.updatedAt
      }) +
      sectionBlock("画像统计", statsGrid({
        "绑定 APP": stats.bindingCount || 0,
        "设备数": stats.deviceCount || 0,
        "登录数": stats.loginCount || 0,
        "启动数": stats.launchCount || 0,
        "订单数": stats.orderCount || 0,
        "支付订单": stats.paidOrderCount || 0,
        "支付金额": formatMoney(stats.paidAmountCents || 0),
        "会员数": stats.memberCount || 0,
        "文件数": stats.fileCount || 0,
        "占用空间": formatBytes(stats.usedBytes || 0)
      })) +
      sectionBlock("绑定 APP", compactTable([
        { title: "APP", key: "appId" },
        { title: "绑定类型", key: "bindType" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        { title: "绑定时间", key: "bindAt" }
      ], profile.bindings || [])) +
      sectionBlock("最近设备", compactTable([
        { title: "ID", key: "id" },
        { title: "APP", key: "appId" },
        { title: "设备码", key: "deviceCode" },
        { title: "状态", render: function (r) { return badge(r.bindStatus); } },
        { title: "绑定时间", key: "bindAt" },
        { title: "最近启动", key: "lastLaunchAt" }
      ], profile.recentDevices || [])) +
      sectionBlock("最近登录", compactTable([
        { title: "时间", key: "createdAt" },
        { title: "APP", key: "appId" },
        { title: "手机号", key: "mobile" },
        { title: "类型", key: "loginType" },
        { title: "结果", render: function (r) { return badge(r.resultStatus); } }
      ], profile.recentLogins || [])) +
      sectionBlock("最近启动", compactTable([
        { title: "时间", key: "createdAt" },
        { title: "APP", key: "appId" },
        { title: "设备", key: "deviceId" },
        { title: "事件", key: "eventType" },
        { title: "版本", key: "version" }
      ], profile.recentLaunches || [])) +
      sectionBlock("最近订单", compactTable([
        { title: "ID", key: "id" },
        { title: "APP", key: "appId" },
        { title: "订单号", key: "orderNo" },
        { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } },
        { title: "状态", render: function (r) { return badge(r.payStatus); } }
      ], profile.recentOrders || [])) +
      sectionBlock("最近会员", compactTable([
        { title: "ID", key: "id" },
        { title: "APP", key: "appId" },
        { title: "主体", key: "memberSubjectType" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        { title: "到期", key: "expireAt" }
      ], profile.recentMembers || [])) +
      sectionBlock("最近文件", compactTable([
        { title: "ID", key: "id" },
        { title: "APP", key: "appId" },
        { title: "路径", key: "virtualPath" },
        { title: "大小", render: function (r) { return formatBytes(r.sizeBytes); } },
        { title: "类型", key: "fileCategory" }
      ], profile.recentFiles || [])) +
      sectionBlock("AI Key / 配额", compactTable([
        { title: "APP", key: "appId" },
        { title: "平台", key: "providerCode" },
        { title: "Key", render: function (r) { return r.apiKey ? "已配置" : "未配置"; } },
        { title: "配额", key: "quotaUnits" }
      ], aiKeys)),
      '<button class="secondary" type="button" onclick="openUserLogs(' + user.id + ', \'' + (user.mobile || "") + '\')">查看登录日志</button>' +
      '<button class="secondary" type="button" onclick="openUserAiUpsertForUser(' + user.id + ')">配置AI Key</button>' +
      '<button type="button" onclick="openUserEdit(' + user.id + ')">编辑</button>' +
      '<button class="secondary" type="button" onclick="openUserPasswordReset(' + user.id + ')">重置密码</button>' +
      '<button class="secondary" type="button" onclick="toggleUser(' + user.id + ', \'' + user.status + '\')">启停</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openUserLogs(userId, mobile) {
  setPage("logs", 0);
  state.logTab = "app-logins";
  setFilters("logs", { appId: "", orderId: "", userId: userId, mobile: mobile || "", adminId: "" });
  closeModal();
  switchView("logs");
}

function openUserEdit(id) {
  api("/admin/users/" + id).then(function (item) {
    openModal("编辑用户", '<div class="form-grid">' +
      input("editUserMobile", "手机号", item.mobile || "") +
      input("editUserUsername", "用户名", item.username || "", { required: false }) +
      input("editUserNickname", "昵称", item.nickname || "", { required: false }) +
      "</div>",
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveUserEdit(' + id + ')">保存</button>');
  }).catch(function (err) { toast(err.message); });
}

function saveUserEdit(id) {
  api("/admin/users/" + id + "/profile", {
    method: "PUT",
    body: {
      mobile: $("editUserMobile").value,
      username: $("editUserUsername").value,
      nickname: $("editUserNickname").value
    }
  }).then(function () {
    toast("用户已更新");
    closeModal();
    renderUsers(currentPage("users"));
  }).catch(function (err) { toast(err.message); });
}

function openUserPasswordReset(id) {
  openModal("重置用户密码", '<div class="form-grid">' +
    input("resetUserPassword", "新密码", "", { type: "password" }) +
    "</div>",
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button class="danger" type="button" onclick="resetUserPassword(' + id + ')">确认重置</button>');
}

function resetUserPassword(id) {
    api("/admin/users/" + id + "/reset-password", {
      method: "POST",
      body: { password: $("resetUserPassword").value }
    }).then(function () {
      toast("密码已重置");
      closeModal();
      renderUsers(currentPage("users"));
    }).catch(function (err) { toast(err.message); });
}

function openUserAiUpsertForUser(userId) {
  loadApps().then(function (apps) {
    openSubModal('配置用户AI Key', '<div class="form-grid">' +
      input('userAiUserId', '用户 ID', userId) +
      select('userAiAppId', 'APP', apps.map(function (a) { return { value: a.appId, label: a.appId + ' / ' + a.appName }; }), apps[0] && apps[0].appId || '') +
      input('userAiProviderCode', 'AI平台', 'api2d') +
      input('userAiQuota', '转入配额', '0', 'number') +
      textarea('userAiKey', '用户 Key', '', { required: false }) +
      '</div>', '<button class="secondary" type="button" onclick="closeSubModal()">取消</button><button type="button" onclick="saveUserAiUpsertAndReturn(' + userId + ')">保存</button>');
  });
}

function saveUserAiUpsertAndReturn(userId) {
  api('/admin/user-ai', { method: 'POST', body: {
    userId: $("userAiUserId").value ? Number($("userAiUserId").value) : null,
    appId: $("userAiAppId").value,
    providerCode: $("userAiProviderCode").value,
    apiKey: $("userAiKey").value,
    quotaUnits: $("userAiQuota").value ? Number($("userAiQuota").value) : 0
  }}).then(function () { closeSubModal(); openUserDetail(userId); }).catch(function (err) { toast(err.message); });
}

function toggleUser(id, status) {
  api("/admin/users/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("用户状态已更新");
    renderUsers(currentPage("users"));
  }).catch(function (err) { toast(err.message); });
}

function renderBindings(page) {
  if (typeof page === "number") setPage("bindings", page);
  page = currentPage("bindings");
  return loadApps().then(function (apps) {
    var filters = queryFilters("bindings");
    setFilters("bindings", {
      appId: filters.appId || "",
      userId: filters.userId || "",
      status: filters.status || ""
    });
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    var createAppOptions = apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    });
    var qs = ["page=" + page, "size=20"];
    if (filters.appId) qs.push("appId=" + encodeURIComponent(filters.appId));
    if (filters.userId) qs.push("userId=" + encodeURIComponent(filters.userId));
    if (filters.status) qs.push("status=" + encodeURIComponent(filters.status));
    return api("/admin/user-bindings?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("bindingAppFilter", "APP", appOptions, filters.appId || "") +
        input("bindingUserFilter", "用户 ID", filters.userId || "") +
        select("bindingStatusFilter", "状态", [
          { value: "", label: "全部状态" },
          optionOf("ENABLED"),
          optionOf("DISABLED")
        ], filters.status || "") +
        '<button class="secondary" type="button" onclick="applyBindingFilter()">筛选</button>' +
        '</div>';
      $("bindings").innerHTML =
        panel("筛选", filterBar) +
        '<div style="height:12px"></div>' +
        panelTitleActions("绑定列表",
          '<button type="button" onclick="openBindingCreate()">新建绑定</button>' +
          '<button class="secondary" type="button" onclick="exportBindings()">导出</button>') +
        table([
          { title: "ID", key: "id" },
          { title: "用户 ID", key: "userId" },
          { title: "APP", key: "appId" },
          { title: "类型", key: "bindType" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "绑定时间", key: "bindAt" },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openBindingDetail(' + r.id + ')">详情</button>' +
                '<button class="small" onclick="toggleBinding(' + r.id + ', \'' + r.status + '\')">启停</button>' +
                '</div>';
            }
          }
        ], rows) +
        renderPager("bindings", pageMeta(data), "renderBindings") +
        '</div>';
    });
  });
}

function applyBindingFilter() {
  setFilters("bindings", {
    appId: $("bindingAppFilter").value,
    userId: $("bindingUserFilter").value,
    status: $("bindingStatusFilter").value
  });
  renderBindings(0);
}

function openBindingCreate() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    var filters = queryFilters("bindings");
    var appOptions = apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    });
    openModal("新建绑定", '<div class="form-grid">' +
      select("bindingCreateAppId", "APP", appOptions, filters.appId || apps[0].appId) +
      input("bindingCreateUserId", "用户 ID") +
      select("bindingCreateType", "绑定类型", [
        optionOf("MOBILE_LOGIN"),
        optionOf("DEVICE_BIND")
      ], "MOBILE_LOGIN") +
      '</div>',
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="createBinding()">创建</button>');
  }).catch(function (err) { toast(err.message); });
}

function createBinding() {
  api("/admin/user-bindings", {
    method: "POST",
    body: {
      appId: $("bindingCreateAppId").value,
      userId: $("bindingCreateUserId").value ? Number($("bindingCreateUserId").value) : null,
      bindType: $("bindingCreateType").value
    }
  }).then(function () {
    toast("绑定已创建");
    closeModal();
    renderBindings(0);
  }).catch(function (err) { toast(err.message); });
}

function openBindingDetail(id) {
  api("/admin/user-bindings/" + id).then(function (item) {
    openModal("绑定详情", detailList({
      "ID": item.id,
      "用户 ID": item.userId,
      "APP": item.appId,
      "绑定类型": item.bindType,
      "状态": item.status,
      "绑定时间": item.bindAt,
      "创建时间": item.createdAt,
      "更新时间": item.updatedAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function toggleBinding(id, status) {
  api("/admin/user-bindings/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("绑定状态已更新");
    renderBindings(currentPage("bindings"));
  }).catch(function (err) { toast(err.message); });
}

function renderDevices(page) {
  if (typeof page === "number") setPage("devices", page);
  page = currentPage("devices");
  return loadApps().then(function (apps) {
    var filters = queryFilters("devices");
    var currentApp = filters.appId || (apps[0] && apps[0].appId) || "";
    setFilters("devices", {
      appId: currentApp,
      userId: filters.userId || "",
      deviceCode: filters.deviceCode || ""
    });
    var qs = ["page=" + page, "size=20", "appId=" + encodeURIComponent(currentApp)];
    if (filters.userId) qs.push("userId=" + encodeURIComponent(filters.userId));
    if (filters.deviceCode) qs.push("deviceCode=" + encodeURIComponent(filters.deviceCode));
    return api("/admin/devices?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      state.deviceRows = rows;
      var filterBar = '<div class="toolbar">' +
        select("deviceAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
        input("deviceUserFilter", "用户 ID", filters.userId || "") +
        input("deviceCodeFilter", "设备码", filters.deviceCode || "") +
        '<button class="secondary" type="button" onclick="applyDeviceFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportDevices()">导出</button>' +
        "</div>";
      $("devices").innerHTML = panel("筛选", filterBar) + '<div style="height:12px"></div>' +
        panel("设备列表", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "设备码", key: "deviceCode" },
          { title: "设备名", key: "deviceName" },
          { title: "类型", key: "deviceType" },
          { title: "用户 ID", key: "userId" },
          { title: "绑定状态", render: function (r) { return badge(r.bindStatus); } },
          { title: "最近启动", key: "lastLaunchAt" },
          {
            title: "操作",
            render: function (r) {
            return '<div class="actions">' +
                '<button class="small" onclick="openDeviceDetail(' + r.id + ')">详情</button>' +
                '<button class="small" onclick="openDeviceCodeEdit(' + r.id + ')">改设备码</button>' +
                '<button class="small" onclick="openDeviceBindUser(' + r.id + ')">绑定用户</button>' +
                '<button class="small danger" onclick="unbindDevice(' + r.id + ')">解绑</button>' +
                '<button class="small ' + (r.bindStatus === "BLACKLISTED" ? "" : "danger") + '" onclick="toggleDeviceBlacklist(' + r.id + ', \'' + r.bindStatus + '\')">' + (r.bindStatus === "BLACKLISTED" ? "解除拉黑" : "拉黑") + '</button>' +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("devices", pageMeta(data), "renderDevices");
    });
  });
}

function applyDeviceFilter() {
  setFilters("devices", {
    appId: $("deviceAppFilter").value,
    userId: $("deviceUserFilter").value,
    deviceCode: $("deviceCodeFilter").value
  });
  renderDevices(0);
}

function openDeviceDetail(id) {
  api("/admin/devices/" + id + "/aggregate").then(function (data) {
    var item = data.device;
    var body = detailList({
      "ID": item.id,
      "APP": item.appId,
      "设备码": item.deviceCode,
      "设备名": item.deviceName,
      "设备类型": item.deviceType,
      "设备指纹": item.deviceFingerprint,
      "用户 ID": item.userId,
      "绑定状态": item.bindStatus,
      "绑定时间": item.bindAt,
      "最近启动": item.lastLaunchAt,
      "会员状态": data.deviceMember ? data.deviceMember.status : "无",
      "会员到期": data.deviceMember ? data.deviceMember.expireAt : "-",
      "总使用时长": data.usageStats ? formatDuration(data.usageStats.totalDurationSeconds) : "-",
      "平均使用时长": data.usageStats ? formatDuration(data.usageStats.averageDurationSeconds) : "-"
    }) + sectionBlock("关键指标", statsGrid(data.stats)) +
      sectionBlock("最近订单", compactTable([
        { title: "ID", key: "id" }, { title: "订单号", key: "orderNo" }, { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } }, { title: "状态", render: function (r) { return badge(r.payStatus); } }
      ], data.recentOrders)) +
      sectionBlock("最近启动", compactTable([
        { title: "ID", key: "id" }, { title: "平台", key: "platform" }, { title: "版本", key: "version" }, { title: "时长", render: function (r) { return formatDuration(r.durationSeconds); } }, { title: "时间", render: function (r) { return formatDateTime(r.createdAt); } }
      ], data.recentLaunches)) +
      sectionBlock("设备码修改历史", compactTable([
        { title: "时间", key: "createdAt" }, { title: "旧设备码", key: "oldDeviceCode" }, { title: "新设备码", key: "newDeviceCode" }, { title: "原因", key: "reason" }, { title: "管理员", key: "adminUsername" }
      ], data.recentDeviceCodeChanges));
    openModal("设备详情", body,
      '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(item.deviceCode || "") + '\')">复制设备码</button>' +
      '<button class="' + (item.bindStatus === "BLACKLISTED" ? "secondary" : "danger") + '" type="button" onclick="toggleDeviceBlacklist(' + item.id + ', \'' + item.bindStatus + '\')">' + (item.bindStatus === "BLACKLISTED" ? "解除拉黑" : "拉黑") + '</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openDeviceCodeEdit(id) {
  var rows = state.deviceRows || [];
  var item = findById(rows, id);
  if (!item) {
    toast("设备不存在");
    return;
  }
  openModal("修改设备码", '<div class="form-grid">' +
    input("editDeviceCode", "设备码", item.deviceCode) +
    '<div class="field-hint">会员权益绑定在 Device ID 上，修改设备码用于管理员处理用户换设备。</div>' +
    '</div>',
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="saveDeviceCodeEdit(' + id + ')">保存</button>');
}

function saveDeviceCodeEdit(id) {
  var nextCode = $("editDeviceCode").value;
  openRiskConfirm("确认修改设备码", "会员权益绑定在 Device ID 上。修改设备码会把该会员资格迁移给新的设备码使用。", "确认修改", function (reason) {
    api("/admin/devices/" + id + "/device-code", {
      method: "PATCH",
      body: { deviceCode: nextCode, reason: reason, confirmReason: reason }
    }).then(function () {
      toast("设备码已更新");
      closeModal();
      renderDevices(currentPage("devices"));
    }).catch(function (err) { toast(err.message); });
  });
}

function openDeviceBindUser(id) {
  openModal("绑定设备用户", '<div class="form-grid">' +
    input("bindDeviceUserId", "用户 ID") +
    '</div>',
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="saveDeviceBindUser(' + id + ')">绑定</button>');
}

function saveDeviceBindUser(id) {
  api("/admin/devices/" + id + "/bind-user", {
    method: "POST",
    body: { userId: $("bindDeviceUserId").value ? Number($("bindDeviceUserId").value) : null }
  }).then(function () {
    toast("设备已绑定用户");
    closeModal();
    renderDevices(currentPage("devices"));
  }).catch(function (err) { toast(err.message); });
}

function unbindDevice(id) {
  openRiskConfirm("确认解绑设备", "解绑会移除设备与用户的绑定关系，但不会删除设备记录。", "确认解绑", function (reason) {
    api("/admin/devices/" + id + "/unbind", { method: "POST", body: { confirmReason: reason } }).then(function () {
      toast("设备已解绑");
      closeModal();
      renderDevices(currentPage("devices"));
    }).catch(function (err) { toast(err.message); });
  });
}

function renderMembers(page) {
  if (typeof page === "number") setPage("members", page);
  page = currentPage("members");
  return loadApps().then(function (apps) {
    var filters = queryFilters("members");
    var currentApp = filters.appId || (apps[0] && apps[0].appId) || "";
    setFilters("members", { appId: currentApp });
    return api("/admin/members?appId=" + encodeURIComponent(currentApp) + "&page=" + page + "&size=20").then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("memberAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
        '<button class="secondary" type="button" onclick="applyMemberFilter()">筛选</button>' +
        '<button type="button" onclick="openGrantMember()">赠送会员</button>' +
        '<button class="secondary" type="button" onclick="exportMembers()">导出</button>' +
        "</div>";
      $("members").innerHTML = panel("筛选与操作", filterBar) + '<div style="height:12px"></div>' +
        panel("会员列表", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "主体", key: "memberSubjectType" },
          { title: "用户", key: "userId" },
          { title: "设备", key: "deviceId" },
          { title: "到期", key: "expireAt" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openMemberDetail(' + r.id + ')">详情</button>' +
                '<button class="small danger" onclick="cancelMember(' + r.id + ')">取消</button>' +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("members", pageMeta(data), "renderMembers");
    });
  });
}

function applyMemberFilter() {
  setFilters("members", { appId: $("memberAppFilter").value });
  renderMembers(0);
}

function openGrantMember() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    openModal("赠送会员", '<div class="form-grid">' +
      select("grantAppId", "APP", apps.map(function (a) {
        return { value: a.appId, label: a.appId + " / " + a.appName };
      }), queryFilters("members").appId || apps[0].appId) +
      select("grantSubject", "主体", [
        optionOf("USER"),
        optionOf("DEVICE")
      ], "USER") +
      input("grantUserId", "用户 ID", "", { required: false, conditional: true, hint: "主体为 USER 时必填" }) +
      input("grantDeviceId", "设备 ID", "", { required: false, conditional: true, hint: "主体为 DEVICE 时必填" }) +
      input("grantPackageId", "套餐 ID") +
      input("grantDays", "天数", "30") +
      "</div>",
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveGrantMember()">赠送</button>');
  }).catch(function (err) { toast(err.message); });
}

function saveGrantMember() {
  api("/admin/members/grant", {
    method: "POST",
    body: {
      appId: $("grantAppId").value,
      subjectType: $("grantSubject").value,
      userId: $("grantUserId").value ? Number($("grantUserId").value) : null,
      deviceId: $("grantDeviceId").value ? Number($("grantDeviceId").value) : null,
      packageId: Number($("grantPackageId").value),
      durationDays: Number($("grantDays").value)
    }
  }).then(function () {
    toast("会员已赠送");
    closeModal();
    renderMembers(currentPage("members"));
  }).catch(function (err) { toast(err.message); });
}

function openMemberDetail(id) {
  api("/admin/members?appId=" + encodeURIComponent(queryFilters("members").appId || "") + "&page=0&size=100").then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("会员不存在");
    openModal("会员详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "主体": item.memberSubjectType,
      "用户": item.userId,
      "设备": item.deviceId,
      "套餐": item.packageId,
      "开始时间": item.startAt,
      "到期时间": item.expireAt,
      "状态": item.status,
      "订单 ID": item.orderId
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function cancelMember(id) {
  openRiskConfirm("确认取消会员", "取消后会员会立即失效，请确认这是管理员手动处理。", "确认取消", function (reason) {
    api("/admin/members/" + id + "/cancel", { method: "POST", body: { confirmReason: reason } }).then(function () {
      toast("会员已取消");
      closeModal();
      renderMembers(currentPage("members"));
    }).catch(function (err) { toast(err.message); });
  });
}

function renderOrders(page) {
  if (typeof page === "number") setPage("orders", page);
  page = currentPage("orders");
  return loadApps().then(function (apps) {
    var filters = queryFilters("orders");
    var currentApp = filters.appId || (apps[0] && apps[0].appId) || "";
    setFilters("orders", { appId: currentApp, keyword: filters.keyword || "" });
    var orderQs = ["appId=" + encodeURIComponent(currentApp), "page=" + page, "size=20"];
    if (filters.keyword) orderQs.push("keyword=" + encodeURIComponent(filters.keyword));
    return api("/admin/orders?" + orderQs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("orderAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
        input("orderKeywordFilter", "订单/设备/交易/手机号", filters.keyword || "", { required: false }) +
        '<button class="secondary" type="button" onclick="applyOrderFilter()">筛选</button>' +
        '<button type="button" onclick="openOrderCreate()">创建订单</button>' +
        '<button class="secondary" type="button" onclick="exportOrders()">导出</button>' +
        "</div>";
      $("orders").innerHTML = panel("筛选", filterBar) + '<div style="height:12px"></div>' +
        panel("订单列表", table([
          { title: "ID", key: "id" },
          { title: "订单号", key: "orderNo" },
          { title: "APP", key: "appId" },
          { title: "用户", key: "userId" },
          { title: "设备", key: "deviceId" },
          { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } },
          { title: "退款(元)", render: function (r) { return formatMoney(r.refundedAmountCents); } },
          { title: "渠道", render: function (r) { return formatValue(r.payChannel); } },
          { title: "状态", render: function (r) { return badge(r.payStatus); } },
          {
            title: "操作",
            render: function (r) {
              var actions = '<button class="small" onclick="openOrderDetail(' + r.id + ')">详情</button>' +
                '<button class="small" onclick="queryChannelOrder(' + r.id + ')">查单</button>';
              if (r.payStatus === "PENDING") {
                actions += '<button class="small" onclick="markPaid(' + r.id + ')">标记支付</button>' +
                  '<button class="small danger" onclick="closeOrder(' + r.id + ')">关闭</button>';
              }
              return '<div class="actions">' +
                actions +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("orders", pageMeta(data), "renderOrders");
    });
  });
}

function applyOrderFilter() {
  setFilters("orders", { appId: $("orderAppFilter").value, keyword: $("orderKeywordFilter").value });
  renderOrders(0);
}

function openOrderCreate() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    openModal("创建订单", '<div class="form-grid">' +
      select("createOrderAppId", "APP", apps.map(function (a) {
        return { value: a.appId, label: a.appId + " / " + a.appName };
      }), queryFilters("orders").appId || apps[0].appId) +
      input("createOrderUserId", "用户 ID", "", { required: false, conditional: true, hint: "账号会员订单时填写" }) +
      input("createOrderDeviceId", "设备 ID", "", { required: false, conditional: true, hint: "设备会员订单时填写" }) +
      input("createOrderPackageId", "套餐 ID") +
      select("createOrderChannel", "支付渠道", paymentChannelOptions(false), "ALIPAY") +
      select("createOrderPayMode", "拉起方式", [
        { value: "QR", label: "扫码支付/二维码" },
        { value: "PAGE", label: "浏览器网页支付" },
        { value: "APP", label: "App SDK 支付" }
      ], "QR") +
      input("createOrderReturnUrl", "同步跳转地址", paymentReturnTemplate(), { required: false }) +
      '</div>',
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveOrderCreate()">创建</button>');
  }).catch(function (err) { toast(err.message); });
}

function saveOrderCreate() {
  api("/api/payment/create-order", {
    method: "POST",
    body: {
      appId: $("createOrderAppId").value,
      userId: $("createOrderUserId").value ? Number($("createOrderUserId").value) : null,
      deviceId: $("createOrderDeviceId").value ? Number($("createOrderDeviceId").value) : null,
      packageId: Number($("createOrderPackageId").value),
      payChannel: $("createOrderChannel").value,
      payMode: $("createOrderPayMode").value,
      returnUrl: $("createOrderReturnUrl").value
    }
  }).then(function (data) {
    toast("订单已创建: " + data.orderNo);
    closeModal();
    openPaymentResult(data);
    renderOrders(0);
  }).catch(function (err) { toast(err.message); });
}

function openPaymentResult(data) {
  var params = data.paymentParams || {};
  var rawPay = params.qrCode || params.payUrl || params.browserUrl || params.orderString || "";
  var publicPayUrl = "/pay.html?orderNo=" + encodeURIComponent(data.orderNo) + "&payUrl=" + encodeURIComponent(rawPay);
  var body = detailList({
    "订单号": data.orderNo,
    "金额(元)": yuanText(data.amountCents),
    "渠道": params.provider,
    "拉起方式": params.payMode,
    "二维码/支付链接": params.qrCode || params.payUrl || params.browserUrl,
    "公开支付页": location.origin + publicPayUrl,
    "App orderString": params.orderString
  });
  var footer = '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(rawPay) + '\')">复制支付参数</button>';
  footer += '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(location.origin + publicPayUrl) + '\')">复制支付页 URL</button>';
  if (rawPay) footer += '<button type="button" onclick="window.open(\'' + escapeHtml(publicPayUrl) + '\', \'_blank\')">打开支付页</button>';
  footer += '<button class="secondary" type="button" onclick="closeModal()">关闭</button>';
  openModal("支付参数", body, footer);
}

function queryChannelOrder(id) {
  api("/admin/orders/" + id + "/channel-query").then(function (data) {
    openModal("渠道查单结果", detailList({
      "支持": data.supported,
      "成功": data.success,
      "交易号": data.tradeNo,
      "状态": data.status,
      "消息": data.message,
      "原始响应": JSON.stringify(data.raw || {})
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openOrderDetail(id) {
  api("/admin/orders/" + id + "/timeline").then(function (data) {
    var item = data.order;
    var body = detailList({
      "ID": item.id,
      "APP": item.appId,
      "用户": item.userId,
      "设备": item.deviceId,
      "套餐": item.packageId,
      "订单号": item.orderNo,
      "金额(元)": yuanText(item.amountCents),
      "支付渠道": item.payChannel,
      "支付提供方": item.payProvider,
      "状态": item.payStatus,
      "交易号": item.tradeNo,
      "渠道订单号": item.channelOrderNo,
      "回调次数": item.callbackCount,
      "退款金额(元)": yuanText(item.refundedAmountCents),
      "支付时间": item.paidAt,
      "过期时间": item.expireAt,
      "关闭时间": item.closedAt,
      "关闭原因": item.closeReason,
      "会员状态": data.member ? data.member.status : "-"
    }) + sectionBlock("订单时间线", compactTable([
      { title: "时间", key: "happenedAt" },
      { title: "事件", key: "title" },
      { title: "状态", render: function (r) { return badge(r.status); } },
      { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } },
      { title: "说明", key: "description" }
    ], data.items));
    openModal("订单详情", body,
      '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(item.orderNo || "") + '\')">复制订单号</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function markPaid(id) {
  openRiskConfirm("确认标记支付", "这会直接把订单改为已支付并触发会员开通/续期，仅用于人工补单。", "确认标记支付", function (reason) {
    api("/admin/orders/" + id + "/mark-paid", {
      method: "POST",
      body: { tradeNo: "MANUAL-" + Date.now(), confirmReason: reason }
    }).then(function () {
      toast("已标记支付");
      closeModal();
      renderOrders(currentPage("orders"));
    }).catch(function (err) { toast(err.message); });
  });
}

function closeOrder(id) {
  openRiskConfirm("确认关闭订单", "关闭后订单不能再支付、标记支付或退款。", "确认关闭", function (reason) {
    api("/admin/orders/" + id + "/close", {
      method: "POST",
      body: { reason: reason, confirmReason: reason }
    }).then(function () {
      toast("订单已关闭");
      closeModal();
      renderOrders(currentPage("orders"));
    }).catch(function (err) { toast(err.message); });
  });
}

function renderRefunds(page) {
  if (typeof page === "number") setPage("refunds", page);
  page = currentPage("refunds");
  return loadApps().then(function (apps) {
    var filters = queryFilters("refunds");
    var currentApp = filters.appId || (apps[0] && apps[0].appId) || "";
    setFilters("refunds", { appId: currentApp });
    return api("/admin/refunds?appId=" + encodeURIComponent(currentApp) + "&page=" + page + "&size=20").then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("refundAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
        '<button class="secondary" type="button" onclick="applyRefundFilter()">筛选</button>' +
        '<button type="button" onclick="openRefundCreate()">申请退款</button>' +
        '<button class="secondary" type="button" onclick="exportRefunds()">导出</button>' +
        "</div>";
      $("refunds").innerHTML = panel("筛选与操作", filterBar) + '<div style="height:12px"></div>' +
        panel("退款列表", table([
          { title: "ID", key: "id" },
          { title: "退款号", key: "refundNo" },
          { title: "订单 ID", key: "orderId" },
          { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "原因", key: "reason" },
          {
            title: "操作",
            render: function (r) {
              var actions = '<button class="small" onclick="openRefundDetail(' + r.id + ')">详情</button>';
              if (r.status === "PENDING") actions += '<button class="small" onclick="channelRefund(' + r.id + ')">原路退款</button>';
              return '<div class="actions">' +
                actions +
                '<button class="small" onclick="refundOk(' + r.id + ')">成功</button>' +
                '<button class="small danger" onclick="refundFail(' + r.id + ')">失败</button>' +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("refunds", pageMeta(data), "renderRefunds");
    });
  });
}

function applyRefundFilter() {
  setFilters("refunds", { appId: $("refundAppFilter").value });
  renderRefunds(0);
}

function openRefundCreate() {
  openModal("申请退款", '<div class="form-grid">' +
    input("refundOrderId", "订单 ID") +
    input("refundAmount", "金额(元)", "", { required: true, type: "number", hint: "单位：元" }) +
    textarea("refundReason", "原因", "人工退款", { required: false }) +
    "</div>",
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="saveRefundCreate()">申请</button>');
}

function saveRefundCreate() {
  api("/admin/refunds", {
    method: "POST",
    body: {
      orderId: Number($("refundOrderId").value),
      amountCents: centsFromYuanInput($("refundAmount").value) || 0,
      reason: $("refundReason").value
    }
  }).then(function () {
    toast("退款已申请");
    closeModal();
    renderRefunds(currentPage("refunds"));
  }).catch(function (err) { toast(err.message); });
}

function openRefundDetail(id) {
  api("/admin/refunds?appId=" + encodeURIComponent(queryFilters("refunds").appId || "") + "&page=0&size=100").then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("退款不存在");
    openModal("退款详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "订单 ID": item.orderId,
      "退款号": item.refundNo,
      "金额(元)": yuanText(item.amountCents),
      "原因": item.reason,
      "状态": item.status,
      "渠道退款号": item.channelRefundNo,
      "处理时间": item.processedAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function channelRefund(id) {
  openRiskConfirm("确认原路退款", "将调用支付渠道退款接口，成功后会自动确认退款并调整会员。", "确认退款", function (reason) {
    api("/admin/refunds/" + id + "/channel-refund", { method: "POST", body: { confirmReason: reason } }).then(function () {
      toast("渠道退款已处理");
      closeModal();
      renderRefunds(currentPage("refunds"));
    }).catch(function (err) { toast(err.message); });
  });
}

function refundOk(id) {
  openRiskConfirm("确认退款成功", "这会把退款单标记为成功，并累计订单已退款金额。", "确认成功", function (reason) {
    api("/admin/refunds/" + id + "/mark-success", {
      method: "POST",
      body: { channelRefundNo: "MANUAL-REFUND-" + Date.now(), confirmReason: reason }
    }).then(function () {
      toast("退款成功");
      closeModal();
      renderRefunds(currentPage("refunds"));
    }).catch(function (err) { toast(err.message); });
  });
}

function refundFail(id) {
  openRiskConfirm("确认退款失败", "这会把退款单标记为失败，后续如需退款需重新申请。", "确认失败", function (reason) {
    api("/admin/refunds/" + id + "/mark-failed", { method: "POST", body: { confirmReason: reason } }).then(function () {
      toast("退款失败已记录");
      closeModal();
      renderRefunds(currentPage("refunds"));
    }).catch(function (err) { toast(err.message); });
  });
}

function renderCallbacks(page) {
  if (typeof page === "number") setPage("callbacks", page);
  page = currentPage("callbacks");
  return loadApps().then(function (apps) {
    var filters = queryFilters("callbacks");
    var currentApp = filters.appId || "";
    var orderId = filters.orderId || "";
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    setFilters("callbacks", { appId: currentApp, orderId: orderId });
    var qs = ["page=" + page, "size=20"];
    if (currentApp) qs.push("appId=" + encodeURIComponent(currentApp));
    if (orderId) qs.push("orderId=" + encodeURIComponent(orderId));
    return api("/admin/payment-callbacks?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("callbackAppFilter", "APP", appOptions, currentApp) +
        input("callbackOrderFilter", "订单 ID", orderId) +
        '<button class="secondary" type="button" onclick="applyCallbackFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportCallbacks()">导出</button>' +
        "</div>";
      $("callbacks").innerHTML = panel("筛选", filterBar) + '<div style="height:12px"></div>' +
        panel("回调列表", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "订单 ID", key: "orderId" },
          { title: "渠道", render: function (r) { return formatValue(r.payChannel); } },
          { title: "提供方", key: "payProvider" },
          { title: "交易号", key: "tradeNo" },
          { title: "验签", render: function (r) { return badge(r.verifyStatus); } },
          { title: "处理", render: function (r) { return badge(r.processStatus); } },
          { title: "时间", key: "createdAt" },
          { title: "操作", render: function (r) { return '<button class="small" onclick="openCallbackDetail(' + r.id + ')">详情</button>'; } }
        ], rows)) +
        renderPager("callbacks", pageMeta(data), "renderCallbacks");
    });
  });
}

function applyCallbackFilter() {
  setFilters("callbacks", {
    appId: $("callbackAppFilter").value,
    orderId: $("callbackOrderFilter").value
  });
  renderCallbacks(0);
}

function openCallbackDetail(id) {
  var filters = queryFilters("callbacks");
  var qs = ["page=0", "size=100"];
  if (filters.appId) qs.push("appId=" + encodeURIComponent(filters.appId));
  if (filters.orderId) qs.push("orderId=" + encodeURIComponent(filters.orderId));
  api("/admin/payment-callbacks?" + qs.join("&")).then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("回调日志不存在");
    openModal("回调详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "订单 ID": item.orderId,
      "支付渠道": item.payChannel,
      "支付提供方": item.payProvider,
      "交易号": item.tradeNo,
      "验签状态": item.verifyStatus,
      "处理状态": item.processStatus,
      "错误": item.errorMessage,
      "原始内容": item.rawPayload,
      "创建时间": item.createdAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function renderLaunches(page) {
  if (typeof page === "number") setPage("launches", page);
  page = currentPage("launches");
  return loadApps().then(function (apps) {
    var filters = queryFilters("launches");
    var currentApp = filters.appId || "";
    setFilters("launches", {
      appId: currentApp,
      deviceId: filters.deviceId || "",
      userId: filters.userId || ""
    });
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    var qs = ["page=" + page, "size=20"];
    if (currentApp) qs.push("appId=" + encodeURIComponent(currentApp));
    if (filters.deviceId) qs.push("deviceId=" + encodeURIComponent(filters.deviceId));
    if (filters.userId) qs.push("userId=" + encodeURIComponent(filters.userId));
    return api("/admin/launch-records?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("launchAppFilter", "APP", appOptions, currentApp) +
        input("launchDeviceFilter", "设备 ID", filters.deviceId || "") +
        input("launchUserFilter", "用户 ID", filters.userId || "") +
        '<button class="secondary" type="button" onclick="applyLaunchFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportLaunches()">导出</button>' +
        "</div>";
      $("launches").innerHTML = panel("筛选", filterBar) + '<div style="height:12px"></div>' +
        panel("启动记录", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "设备 ID", key: "deviceId" },
          { title: "用户 ID", key: "userId" },
          { title: "平台", key: "platform" },
          { title: "分支", key: "branch" },
          { title: "渠道", key: "channel" },
          { title: "环境", key: "platformEnvironment" },
          { title: "版本名", key: "versionName" },
          { title: "版本号", key: "versionCode" },
          { title: "版本", key: "version" },
          { title: "网络", key: "networkType" },
          { title: "事件", render: function (r) { return launchEventLabel(r); } },
          { title: "开始时间", render: function (r) { return formatDateTime(r.sessionStartAt || r.createdAt); } },
          { title: "结束时间", render: function (r) { return formatDateTime(r.sessionEndAt); } },
          { title: "时长", render: function (r) { return formatDuration(r.durationSeconds); } },
          { title: "操作", render: function (r) { return '<button class="small" onclick="openLaunchDetail(' + r.id + ')">详情</button>'; } }
        ], rows)) +
        renderPager("launches", pageMeta(data), "renderLaunches");
    });
  });
}

function launchEventLabel(record) {
  var text = formatValue(record.eventType);
  if (record.eventType === "LAUNCH" && record.sessionId) {
    return '<span title="会话 ID：' + escapeHtml(record.sessionId) + '">' + text + '</span>';
  }
  return text;
}

function applyLaunchFilter() {
  setFilters("launches", {
    appId: $("launchAppFilter").value,
    deviceId: $("launchDeviceFilter").value,
    userId: $("launchUserFilter").value
  });
  renderLaunches(0);
}

function openLaunchDetail(id) {
  api("/admin/launch-records/" + id).then(function (item) {
    var details = {
      "ID": item.id,
      "APP": item.appId,
      "设备 ID": item.deviceId,
      "用户 ID": item.userId,
      "平台": item.platform,
      "分支": item.branch,
      "渠道": item.channel,
      "环境": item.platformEnvironment,
      "版本名": item.versionName,
      "版本号": item.versionCode,
      "版本": item.version,
      "网络": item.networkType,
      "IP": item.ipAddress,
      "事件": item.eventType,
      "开始时间": formatDateTime(item.sessionStartAt || item.createdAt),
      "结束时间": formatDateTime(item.sessionEndAt),
      "使用时长": formatDuration(item.durationSeconds),
      "内容": item.eventData,
      "记录时间": formatDateTime(item.createdAt)
    };
    if (item.eventType === "LAUNCH" && item.sessionId) details["会话 ID"] = item.sessionId;
    openModal("启动详情", detailList(details), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function renderAdapterReports(page) {
  if (typeof page === "number") setPage("adapter", page);
  page = currentPage("adapter");
  return loadApps().then(function (apps) {
    var filters = queryFilters("adapter");
    var currentApp = filters.appId || "";
    setFilters("adapter", {
      appId: currentApp,
      sourceId: filters.sourceId || ""
    });
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    var qs = ["page=" + page, "size=20"];
    if (currentApp) qs.push("appId=" + encodeURIComponent(currentApp));
    if (filters.sourceId) qs.push("sourceId=" + encodeURIComponent(filters.sourceId));
    return api("/admin/adapter-reports?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("adapterAppFilter", "APP", appOptions, currentApp) +
        input("adapterSourceFilter", "来源 ID", filters.sourceId || "") +
        '<button class="secondary" type="button" onclick="applyAdapterFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportAdapterReports()">导出</button>' +
        "</div>";
      $("adapter").innerHTML = panel("筛选", filterBar) + '<div style="height:12px"></div>' +
        panel("适配上报", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "来源", key: "sourceId" },
          { title: "类型", key: "reportType" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "时间", key: "createdAt" },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openAdapterDetail(' + r.id + ')">详情</button>' +
                '<button class="small" onclick="markAdapterProcessed(' + r.id + ')">处理完成</button>' +
                '<button class="small danger" onclick="markAdapterFailed(' + r.id + ')">失败</button>' +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("adapter", pageMeta(data), "renderAdapterReports");
    });
  });
}

function applyAdapterFilter() {
  setFilters("adapter", {
    appId: $("adapterAppFilter").value,
    sourceId: $("adapterSourceFilter").value
  });
  renderAdapterReports(0);
}

function openAdapterDetail(id) {
  api("/admin/adapter-reports/" + id).then(function (item) {
    openModal("适配上报详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "来源 ID": item.sourceId,
      "类型": item.reportType,
      "状态": item.status,
      "内容": item.payload,
      "时间": item.createdAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function markAdapterProcessed(id) {
  api("/admin/adapter-reports/" + id + "/mark-processed", { method: "POST" }).then(function () {
    toast("已标记处理完成");
    renderAdapterReports(currentPage("adapter"));
  }).catch(function (err) { toast(err.message); });
}

function markAdapterFailed(id) {
  api("/admin/adapter-reports/" + id + "/mark-failed", { method: "POST" }).then(function () {
    toast("已标记失败");
    renderAdapterReports(currentPage("adapter"));
  }).catch(function (err) { toast(err.message); });
}

function renderLogs(page) {
  if (typeof page === "number") setPage("logs", page);
  page = currentPage("logs");
  return loadApps().then(function (apps) {
    var filters = queryFilters("logs");
    var appId = filters.appId || "";
    var orderId = filters.orderId || "";
    var userId = filters.userId || "";
    var adminId = filters.adminId || "";
    var mobile = filters.mobile || "";
    var appOptions = [{ value: "", label: "全部 APP" }].concat(apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    }));
    var bar = '<div class="toolbar">' +
      select("logTab", "日志类型", [
        { value: "admin-operations", label: "后台操作" },
        { value: "app-logins", label: "APP 登录" },
        { value: "launches", label: "启动日志" },
        { value: "payment-events", label: "支付事件" }
      ], state.logTab) +
      select("logAppFilter", "APP", appOptions, appId) +
      input("logOrderFilter", "订单 ID", orderId) +
      input("logUserFilter", "用户 ID", userId) +
      input("logMobileFilter", "手机号", mobile) +
      input("logAdminFilter", "管理员 ID", adminId) +
      '<button class="secondary" type="button" onclick="applyLogFilter()">筛选</button>' +
      '<button class="secondary" type="button" onclick="exportLogs()">导出</button>' +
      "</div>";
    $("logs").innerHTML = panel("筛选", bar) + '<div style="height:12px"></div>' +
      panel("日志列表", '<div id="logTable"></div>' + '<div id="logPager"></div>');
    return loadLogData(page);
  });
}

function applyLogFilter() {
  state.logTab = $("logTab").value;
  setFilters("logs", {
    appId: $("logAppFilter").value,
    orderId: $("logOrderFilter").value,
    userId: $("logUserFilter").value,
    mobile: $("logMobileFilter").value,
    adminId: $("logAdminFilter").value
  });
  renderLogs(0);
}

function loadLogData(page) {
  var filters = queryFilters("logs");
  var qs = ["page=" + (typeof page === "number" ? page : currentPage("logs")), "size=20"];
  if (filters.appId) qs.push("appId=" + encodeURIComponent(filters.appId));
  if (filters.orderId) qs.push("orderId=" + encodeURIComponent(filters.orderId));
  if (filters.userId) qs.push("userId=" + encodeURIComponent(filters.userId));
  if (filters.mobile) qs.push("mobile=" + encodeURIComponent(filters.mobile));
  if (filters.adminId) qs.push("adminId=" + encodeURIComponent(filters.adminId));
  var pathMap = {
    "admin-operations": "/admin/logs/admin-operations",
    "app-logins": "/admin/logs/app-logins",
    "launches": "/admin/logs/launches",
    "payment-events": "/admin/logs/payment-events"
  };
  return api(pathMap[state.logTab] + "?" + qs.join("&")).then(function (data) {
    var rows = pageContent(data);
    var columnsMap = {
      "admin-operations": [
        { title: "时间", key: "createdAt" },
        { title: "管理员", key: "adminId" },
        { title: "用户名", key: "username" },
        { title: "操作", key: "operationType" },
        { title: "URI", key: "requestUri" },
        { title: "结果", render: function (r) { return badge(r.resultStatus); } },
        { title: "操作", render: function (r) { return logDetailButton("admin-operations", r.id); } }
      ],
      "app-logins": [
        { title: "时间", key: "createdAt" },
        { title: "APP", key: "appId" },
        { title: "手机号", key: "mobile" },
        { title: "用户 ID", key: "userId" },
        { title: "结果", render: function (r) { return badge(r.resultStatus); } },
        { title: "操作", render: function (r) { return logDetailButton("app-logins", r.id); } }
      ],
      "launches": [
        { title: "时间", key: "createdAt" },
        { title: "APP", key: "appId" },
        { title: "设备", key: "deviceId" },
        { title: "用户", key: "userId" },
        { title: "事件", key: "eventType" },
        { title: "版本", key: "version" },
        { title: "操作", render: function (r) { return logDetailButton("launches", r.id); } }
      ],
      "payment-events": [
        { title: "时间", key: "createdAt" },
        { title: "APP", key: "appId" },
        { title: "订单", key: "orderId" },
        { title: "事件", key: "eventType" },
        { title: "操作方", key: "operatorType" },
        { title: "金额(元)", render: function (r) { return formatMoney(r.amountCents); } },
        { title: "操作", render: function (r) { return logDetailButton("payment-events", r.id); } }
      ]
    };
    $("logTable").innerHTML = table(columnsMap[state.logTab], rows);
    $("logPager").innerHTML = renderPager("logs", pageMeta(data), "renderLogs");
    return null;
  }).catch(function (err) { toast(err.message); });
}

function logDetailButton(type, id) {
  return '<button class="small" onclick="openLogDetail(\'' + type + '\',' + id + ')">详情</button>';
}

function openLogDetail(type, id) {
  var filters = queryFilters("logs");
  var qs = ["page=0", "size=100"];
  if (filters.appId) qs.push("appId=" + encodeURIComponent(filters.appId));
  if (filters.orderId) qs.push("orderId=" + encodeURIComponent(filters.orderId));
  if (filters.userId) qs.push("userId=" + encodeURIComponent(filters.userId));
  if (filters.mobile) qs.push("mobile=" + encodeURIComponent(filters.mobile));
  if (filters.adminId) qs.push("adminId=" + encodeURIComponent(filters.adminId));
  var pathMap = {
    "admin-operations": "/admin/logs/admin-operations",
    "app-logins": "/admin/logs/app-logins",
    "launches": "/admin/logs/launches",
    "payment-events": "/admin/logs/payment-events"
  };
  api(pathMap[type] + "?" + qs.join("&")).then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("日志不存在");
    var details = {
      "ID": item.id,
      "时间": item.createdAt,
      "APP": item.appId
    };
    if (type === "admin-operations") {
      details["管理员 ID"] = item.adminId;
      details["用户名"] = item.username;
      details["操作"] = item.operationType;
      details["URI"] = item.requestUri;
      details["方法"] = item.requestMethod;
      details["结果"] = item.resultStatus;
      details["参数"] = item.requestBody;
      details["错误"] = item.errorMessage;
    } else if (type === "app-logins") {
      details["手机号"] = item.mobile;
      details["用户 ID"] = item.userId;
      details["登录类型"] = item.loginType;
      details["设备 ID"] = item.deviceId;
      details["设备码"] = item.deviceCode;
      details["IP"] = item.ipAddress;
      details["UA"] = item.userAgent;
      details["结果"] = item.resultStatus;
      details["错误"] = item.errorMessage;
    } else if (type === "launches") {
      details["设备 ID"] = item.deviceId;
      details["用户 ID"] = item.userId;
      details["平台"] = item.platform;
      details["版本"] = item.version;
      details["网络"] = item.networkType;
      details["IP"] = item.ipAddress;
      details["事件"] = item.eventType;
      details["内容"] = item.eventData;
    } else if (type === "payment-events") {
      details["订单 ID"] = item.orderId;
      details["事件"] = item.eventType;
      details["支付渠道"] = item.payChannel;
      details["支付提供方"] = item.payProvider;
      details["金额(元)"] = item.amountCents;
      details["交易号"] = item.tradeNo;
      details["操作方"] = item.operatorType;
      details["操作方 ID"] = item.operatorId;
      details["内容"] = item.eventData;
    }
    openModal("日志详情", detailList(details), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function exportPaymentConfigs() {
  var f = queryFilters("paymentConfigs");
  exportCsv("/admin/exports/payment-configs" + queryString({
    appId: f.appId,
    payChannel: f.payChannel,
    status: f.status,
    limit: 5000
  }), "payment-configs.csv");
}

function exportNotificationConfigs(type) {
  var viewKey = notificationViewKey(type);
  var f = queryFilters(viewKey);
  exportCsv("/admin/exports/notification-configs" + queryString({
    channelType: type,
    providerCode: f.providerCode,
    status: f.status,
    limit: 5000
  }), (type === "SMS" ? "sms-configs.csv" : "email-configs.csv"));
}

function exportPackages() {
  var f = queryFilters("packages");
  exportCsv("/admin/exports/packages" + queryString({ appId: f.appId, limit: 5000 }), "packages.csv");
}

function exportUsers() {
  var f = queryFilters("users");
  exportCsv("/admin/exports/users" + queryString({
    keyword: f.keyword,
    mobile: f.mobile,
    username: f.username,
    limit: 5000
  }), "users.csv");
}

function toggleDeviceBlacklist(id, status) {
  var blacklisted = status === "BLACKLISTED";
  var title = blacklisted ? "确认解除设备黑名单" : "确认拉黑设备";
  var message = blacklisted ? "解除后设备可以重新注册、启动和刷新登录。"
    : "拉黑后该设备无法注册、启动、刷新登录，且该设备的 refresh token 会被撤销。";
  openRiskConfirm(title, message, blacklisted ? "确认解除" : "确认拉黑", function () {
    api("/admin/devices/" + id + "/" + (blacklisted ? "unblacklist" : "blacklist"), { method: "POST" }).then(function () {
      toast(blacklisted ? "设备已解除拉黑" : "设备已拉黑");
      closeModal();
      renderDevices(currentPage("devices"));
    }).catch(function (err) { toast(err.message); });
  });
}

function exportBindings() {
  var f = queryFilters("bindings");
  exportCsv("/admin/exports/user-bindings" + queryString({
    appId: f.appId,
    userId: f.userId,
    status: f.status,
    limit: 5000
  }), "user-bindings.csv");
}

function exportDevices() {
  var f = queryFilters("devices");
  exportCsv("/admin/exports/devices" + queryString({
    appId: f.appId,
    userId: f.userId,
    deviceCode: f.deviceCode,
    limit: 5000
  }), "devices.csv");
}

function exportMembers() {
  var f = queryFilters("members");
  exportCsv("/admin/exports/members" + queryString({ appId: f.appId, limit: 5000 }), "members.csv");
}

function exportOrders() {
  var f = queryFilters("orders");
  exportCsv("/admin/exports/orders" + queryString({ appId: f.appId, limit: 5000 }), "orders.csv");
}

function exportRefunds() {
  var f = queryFilters("refunds");
  exportCsv("/admin/exports/payment-refunds" + queryString({ appId: f.appId, limit: 5000 }), "payment-refunds.csv");
}

function exportCallbacks() {
  var f = queryFilters("callbacks");
  exportCsv("/admin/exports/payment-callbacks" + queryString({
    appId: f.appId,
    orderId: f.orderId,
    limit: 5000
  }), "payment-callbacks.csv");
}

function exportLaunches() {
  var f = queryFilters("launches");
  exportCsv("/admin/exports/launch-records" + queryString({
    appId: f.appId,
    deviceId: f.deviceId,
    userId: f.userId,
    limit: 5000
  }), "launch-records.csv");
}

function exportAdapterReports() {
  var f = queryFilters("adapter");
  exportCsv("/admin/exports/adapter-reports" + queryString({
    appId: f.appId,
    sourceId: f.sourceId,
    limit: 5000
  }), "adapter-reports.csv");
}

function exportLogs() {
  var f = queryFilters("logs");
  var pathMap = {
    "admin-operations": "/admin/exports/logs/admin-operations",
    "app-logins": "/admin/exports/logs/app-logins",
    "launches": "/admin/exports/logs/launches",
    "payment-events": "/admin/exports/logs/payment-events"
  };
  exportCsv(pathMap[state.logTab] + queryString({
    appId: f.appId,
    orderId: f.orderId,
    userId: f.userId,
    mobile: f.mobile,
    adminId: f.adminId,
    limit: 5000
  }), state.logTab + ".csv");
}

function exportAdmins() {
  var f = queryFilters("admins");
  exportCsv("/admin/exports/admin-users" + queryString({
    username: f.username,
    limit: 5000
  }), "admin-users.csv");
}

function renderAdmins(page) {
  if (typeof page === "number") setPage("admins", page);
  page = currentPage("admins");
  var filters = queryFilters("admins");
  setFilters("admins", { username: filters.username || "" });
  var qs = ["page=" + page, "size=20"];
  if (filters.username) qs.push("username=" + encodeURIComponent(filters.username));
  return api("/admin/admin-users?" + qs.join("&")).then(function (data) {
    var rows = pageContent(data);
    var passwordBody = '<div class="form-grid">' +
      input("ownOldPassword", "原密码", "", "password") +
      input("ownNewPassword", "新密码", "", { type: "password", hint: "至少 6 位" }) +
      '<button type="button" onclick="changeOwnPassword()">修改密码</button>' +
      '</div>';
    var filterBar = '<div class="toolbar">' +
      input("adminUsernameFilter", "用户名", filters.username || "") +
      '<button class="secondary" type="button" onclick="applyAdminFilter()">筛选</button>' +
      '</div>';
    $("admins").innerHTML =
      panel("修改当前密码", passwordBody) +
      '<div style="height:12px"></div>' +
      panel("筛选", filterBar) +
      '<div style="height:12px"></div>' +
      panelTitleActions("管理员列表",
        '<button type="button" onclick="openAdminCreate()">新建管理员</button>' +
        '<button class="secondary" type="button" onclick="exportAdmins()">导出</button>') +
      table([
        { title: "ID", key: "id" },
        { title: "用户名", key: "username" },
        { title: "显示名", key: "displayName" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        { title: "最后登录", key: "lastLoginAt" },
        { title: "创建时间", key: "createdAt" },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openAdminEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="toggleAdminUser(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '<button class="small danger" onclick="openAdminResetPassword(' + r.id + ')">重置密码</button>' +
              '</div>';
          }
        }
      ], rows) +
      renderPager("admins", pageMeta(data), "renderAdmins") +
      '</div>';
  });
}

function applyAdminFilter() {
  setFilters("admins", { username: $("adminUsernameFilter").value });
  renderAdmins(0);
}

function openAdminCreate() {
  openModal("新建管理员", '<div class="form-grid">' +
    input("adminUsername", "用户名") +
    input("adminDisplayName", "显示名") +
    input("adminPassword", "初始密码", "", { type: "password", hint: "至少 6 位" }) +
    '</div>',
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="createAdminUser()">创建</button>');
}

function createAdminUser() {
  api("/admin/admin-users", {
    method: "POST",
    body: {
      username: $("adminUsername").value.trim(),
      displayName: $("adminDisplayName").value.trim(),
      password: $("adminPassword").value
    }
  }).then(function () {
    toast("管理员已创建");
    closeModal();
    renderAdmins(0);
  }).catch(function (err) { toast(err.message); });
}

function openAdminEdit(id) {
  api("/admin/admin-users/" + id).then(function (item) {
    openModal("编辑管理员", '<div class="form-grid">' +
      input("editAdminDisplayName", "显示名", item.displayName) +
      '</div>',
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveAdminEdit(' + id + ')">保存</button>');
  }).catch(function (err) { toast(err.message); });
}

function saveAdminEdit(id) {
  api("/admin/admin-users/" + id, {
    method: "PUT",
    body: { displayName: $("editAdminDisplayName").value.trim() }
  }).then(function () {
    toast("管理员已更新");
    closeModal();
    renderAdmins(currentPage("admins"));
  }).catch(function (err) { toast(err.message); });
}

function toggleAdminUser(id, status) {
  api("/admin/admin-users/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("管理员状态已更新");
    renderAdmins(currentPage("admins"));
  }).catch(function (err) { toast(err.message); });
}

function openAdminResetPassword(id) {
  openModal("重置管理员密码", '<div class="form-grid">' +
    input("resetAdminPassword", "新密码", "", { type: "password", hint: "至少 6 位" }) +
    '</div>',
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button class="danger" type="button" onclick="saveAdminResetPassword(' + id + ')">重置</button>');
}

function saveAdminResetPassword(id) {
  api("/admin/admin-users/" + id + "/reset-password", {
    method: "POST",
    body: { newPassword: $("resetAdminPassword").value }
  }).then(function () {
    toast("密码已重置");
    closeModal();
  }).catch(function (err) { toast(err.message); });
}

function openMustChangePasswordModal(message) {
  openModal("必须修改默认密码", '<p class="muted">' + escapeHtml(message || "默认管理员密码必须先修改") + '</p><div class="form-grid">' +
    input("ownOldPassword", "当前密码", "", "password") +
    input("ownNewPassword", "新密码", "", { type: "password", hint: "至少 6 位" }) +
    '</div>',
    '<button type="button" onclick="changeOwnPassword()">修改密码</button>');
}

function changeOwnPassword() {
  api("/admin/admin-users/me/change-password", {
    method: "POST",
    body: {
      oldPassword: $("ownOldPassword").value,
      newPassword: $("ownNewPassword").value
    }
  }).then(function () {
    toast("密码已修改，请重新登录");
    window.setTimeout(logout, 900);
  }).catch(function (err) { toast(err.message); });
}

var platformPolicyCategories = ["AI", "PAYMENT", "SMS", "EMAIL", "CAPTCHA"];

function policyDefaultJson(category) {
  if (category === "PAYMENT") return '{"defaultPayChannel":"ALIPAY"}';
  if (category === "SMS") return '{"cooldownSeconds":60,"expireMinutes":5}';
  if (category === "CAPTCHA") return '{"ttlSeconds":300,"length":6,"maxAttempts":5,"debugReturnCode":false}';
  if (category === "AI") return '{"defaultModel":"","dailyLimitUnits":0}';
  return '{}';
}

function renderPlatformPolicies() {
  return loadApps().then(function (apps) {
    var currentApp = queryFilters("platformPolicies").appId || (apps[0] && apps[0].appId) || "";
    setFilters("platformPolicies", { appId: currentApp });
    if (!currentApp) {
      $("platformPolicies").innerHTML = panel("APP 覆盖策略", '<p class="muted">请先创建 APP。</p>');
      return;
    }
    return api("/admin/app-platform-policies?appId=" + encodeURIComponent(currentApp)).then(function (rows) {
      rows = rows || [];
      $("platformPolicies").innerHTML =
        panelTitleActions("APP 覆盖策略",
          '<button type="button" onclick="openPlatformPolicyUpsert()">配置策略</button>') +
        '<div class="toolbar">' +
        select("platformPolicyAppId", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
        '<button class="secondary" type="button" onclick="applyPlatformPolicyApp()">切换</button>' +
        '</div>' +
        table([
          { title: "类别", render: function (r) { return labelOf(r.category); } },
          { title: "供应商/默认项", key: "providerCode" },
          { title: "策略", key: "policyJson" },
          { title: "普通配置", key: "configJson" },
          { title: "敏感配置", render: function (r) { return r.credentialConfigured ? "已配置" : "未配置"; } },
          { title: "状态", render: function (r) { return badge(r.enabled ? "ENABLED" : "DISABLED"); } },
          { title: "操作", render: function (r) { return '<div class="actions"><button class="small" onclick="openPlatformPolicyUpsert(\'' + r.category + '\')">编辑</button></div>'; } }
        ], rows) + '</div>';
    });
  }).catch(function (err) { toast(err.message); });
}

function applyPlatformPolicyApp() {
  setFilters("platformPolicies", { appId: $("platformPolicyAppId").value });
  renderPlatformPolicies();
}

function openPlatformPolicyUpsert(category) {
  loadApps().then(function (apps) {
    var currentApp = queryFilters("platformPolicies").appId || (apps[0] && apps[0].appId) || "";
    return api("/admin/app-platform-policies?appId=" + encodeURIComponent(currentApp)).then(function (rows) {
      var item = (rows || []).filter(function (r) { return !category || r.category === category; })[0] || {};
      var safeCategory = category || item.category || "SMS";
      openModal("配置 APP 覆盖策略", '<div class="form-grid">' +
        select("policyAppId", "APP", apps.map(function (a) { return { value: a.appId, label: a.appId + " / " + a.appName }; }), currentApp) +
        select("policyCategory", "类别", platformPolicyCategories.map(optionOf), safeCategory) +
        checkbox("policyEnabled", "启用", item.enabled !== false) +
        input("policyProvider", "供应商/默认提供方", item.providerCode || "", { required: false }) +
        textarea("policyPolicyJson", "策略 JSON", item.policyJson || policyDefaultJson(safeCategory), { required: false }) +
        textarea("policyConfigJson", "普通配置 JSON", item.configJson || "{}", { required: false }) +
        textarea("policyCredentialJson", "敏感配置 JSON（留空不修改）", "", { required: false }) +
        '</div>', '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="savePlatformPolicy()">保存</button>');
      $("policyCategory").addEventListener("change", function () {
        if (!$("policyPolicyJson").value || $("policyPolicyJson").value === "{}") {
          $("policyPolicyJson").value = policyDefaultJson($("policyCategory").value);
        }
      });
    });
  }).catch(function (err) { toast(err.message); });
}

function savePlatformPolicy() {
  api("/admin/app-platform-policies", {
    method: "POST",
    body: {
      appId: $("policyAppId").value,
      category: $("policyCategory").value,
      enabled: $("policyEnabled").checked,
      providerCode: $("policyProvider").value,
      policyJson: $("policyPolicyJson").value,
      configJson: $("policyConfigJson").value,
      credentialJson: $("policyCredentialJson").value
    }
  }).then(function () {
    toast("APP 覆盖策略已保存");
    closeModal();
    setFilters("platformPolicies", { appId: $("policyAppId").value });
    renderPlatformPolicies();
  }).catch(function (err) { toast(err.message); });
}

function renderStorageConfigs() {
  return api("/admin/storage").then(function (data) {
    var backend = data.backend || "local";
    var rows = [{
      name: "本地",
      backend: "local",
      status: backend === "local" ? "ENABLED" : "DISABLED",
      path: data.localPath || "./storage",
      resolvedPath: data.resolvedLocalPath || "",
      baseUrl: data.localBaseUrl || ""
    }];
    $("storageConfigs").innerHTML =
      panel("当前存储", detailList({
        "当前后端": backend === "local" ? "本地" : backend,
        "默认路径": data.localPath || "./storage",
        "实际路径": data.resolvedLocalPath || "-",
        "访问地址": data.localBaseUrl || "-",
        "单个配置上限": formatBytes(data.maxConfigFileBytes),
        "单张图片上限": formatBytes(data.maxImageFileBytes),
        "默认用户空间": formatBytes(data.defaultQuotaBytes),
        "默认文件数上限": data.maxFileCount
      })) +
      '<div style="height:12px"></div>' +
      panel("存储后端", table([
        { title: "名称", key: "name" },
        { title: "后端", key: "backend" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        { title: "配置路径", key: "path" },
        { title: "实际路径", key: "resolvedPath" },
        { title: "访问地址", key: "baseUrl" }
      ], rows));
  });
}

function renderSearchPlatforms() {
  return api("/admin/search-platforms").then(function (rows) {
    rows = rows || [];
    $("searchPlatforms").innerHTML =
      panelTitleActions("搜索平台列表",
        '<button type="button" onclick="openSearchPlatformCreate()">新建平台</button>') +
      table([
        { title: "编码", key: "providerCode" },
        { title: "名称", key: "displayName" },
        { title: "Base URL", key: "baseUrl" },
        { title: "后台 Base URL", key: "consoleBaseUrl" },
        { title: "状态", render: function (r) { return badge(r.enabled ? "ENABLED" : "DISABLED"); } },
        { title: "操作", render: function (r) {
          return '<div class="actions">' +
            '<button class="small" onclick="openSearchPlatformEdit(' + r.id + ')">配置</button>' +
            '<button class="small" onclick="toggleSearchPlatform(' + r.id + ', ' + (r.enabled ? "true" : "false") + ')">启停</button>' +
            '<button class="small danger" onclick="deleteSearchPlatform(' + r.id + ')">删除</button>' +
            '</div>';
        } }
      ], rows) + '</div>';
  });
}

function openSearchPlatformCreate() {
  var defaults = searchPlatformDefaults.bocha;
  openModal("新建搜索平台", '<div class="form-grid">' +
    select("searchProviderCode", "支持的平台", Object.keys(searchPlatformDefaults).map(optionOf), "bocha") +
    input("searchProviderName", "显示名称", defaults.displayName) +
    input("searchProviderBaseUrl", "API Base URL", defaults.baseUrl, { required: false }) +
    input("searchProviderConsoleBaseUrl", "控制台 Base URL", defaults.consoleBaseUrl, { required: false }) +
    input("searchProviderEndpointPath", "接口路径", defaults.endpointPath, { required: false }) +
    input("searchProviderDefaultCount", "默认条数", defaults.defaultCount, { required: false, type: "number" }) +
    input("searchProviderTimeoutSeconds", "超时秒数", defaults.timeoutSeconds, { required: false, type: "number" }) +
    input("searchProviderFreshness", "时间范围", defaults.freshness, { required: false }) +
    textarea("searchProviderApiKey", "API Key", "", { required: false }) +
    '</div>', '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveSearchPlatformCreate()">保存</button>');
  bindSearchPlatformDefault();
}

function saveSearchPlatformCreate() {
  api("/admin/search-platforms", {
    method: "POST",
    body: {
      providerCode: $("searchProviderCode").value,
      displayName: $("searchProviderName").value,
      baseUrl: $("searchProviderBaseUrl").value,
      consoleBaseUrl: $("searchProviderConsoleBaseUrl").value,
      configJson: searchPlatformConfigJson(),
      credentialJson: searchPlatformCredentialJson()
    }
  }).then(function () {
    closeModal();
    renderSearchPlatforms();
  }).catch(function (err) { toast(err.message); });
}

function openSearchPlatformEdit(id) {
  api("/admin/search-platforms/" + id).then(function (item) {
    var config = parseJsonObject(item.configJson || "{}");
    var credential = parseJsonObject(item.credentialJson || "{}");
    var defaults = searchPlatformDefaults[item.providerCode] || searchPlatformDefaults.bocha;
    openModal("编辑搜索平台", '<div class="form-grid">' +
      input("searchProviderCode", "平台编码", item.providerCode) +
      input("searchProviderName", "显示名称", item.displayName) +
      input("searchProviderBaseUrl", "API Base URL", item.baseUrl || "", { required: false }) +
      input("searchProviderConsoleBaseUrl", "控制台 Base URL", item.consoleBaseUrl || "", { required: false }) +
      input("searchProviderEndpointPath", "接口路径", config.endpointPath || defaults.endpointPath || "", { required: false }) +
      input("searchProviderDefaultCount", "默认条数", config.defaultCount || defaults.defaultCount || 10, { required: false, type: "number" }) +
      input("searchProviderTimeoutSeconds", "超时秒数", config.timeoutSeconds || defaults.timeoutSeconds || 30, { required: false, type: "number" }) +
      input("searchProviderFreshness", "时间范围", config.freshness || "", { required: false }) +
      textarea("searchProviderApiKey", "API Key（留空不修改）", credential.apiKey || "", { required: false }) +
      '</div>', '<button class="danger" type="button" onclick="deleteSearchPlatform(' + id + ')">删除</button><button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveSearchPlatformEdit(' + id + ')">保存</button>');
    var code = $("searchProviderCode"); if (code) code.disabled = true;
  }).catch(function (err) { toast(err.message); });
}

function saveSearchPlatformEdit(id) {
  api("/admin/search-platforms/" + id, {
    method: "PUT",
    body: {
      providerCode: $("searchProviderCode").value,
      displayName: $("searchProviderName").value,
      baseUrl: $("searchProviderBaseUrl").value,
      consoleBaseUrl: $("searchProviderConsoleBaseUrl").value,
      configJson: searchPlatformConfigJson(),
      credentialJson: searchPlatformCredentialJson()
    }
  }).then(function () {
    closeModal();
    renderSearchPlatforms();
  }).catch(function (err) { toast(err.message); });
}

function toggleSearchPlatform(id, enabled) {
  api("/admin/search-platforms/" + id + "/status", {
    method: "PATCH",
    body: { enabled: !enabled }
  }).then(function () {
    toast("搜索平台状态已更新");
    renderSearchPlatforms();
  }).catch(function (err) { toast(err.message); });
}

function deleteSearchPlatform(id) {
  if (!confirm("确定删除这个搜索平台配置？")) return;
  api("/admin/search-platforms/" + id, { method: "DELETE" }).then(function () {
    toast("搜索平台配置已删除");
    closeModal();
    renderSearchPlatforms();
  }).catch(function (err) { toast(err.message); });
}

function bindSearchPlatformDefault() {
  var provider = $("searchProviderCode");
  if (!provider) return;
  provider.addEventListener("change", function () {
    var d = searchPlatformDefaults[provider.value] || {};
    $("searchProviderName").value = d.displayName || labelOf(provider.value);
    $("searchProviderBaseUrl").value = d.baseUrl || "";
    $("searchProviderConsoleBaseUrl").value = d.consoleBaseUrl || "";
    $("searchProviderEndpointPath").value = d.endpointPath || "";
    $("searchProviderDefaultCount").value = d.defaultCount || 10;
    $("searchProviderTimeoutSeconds").value = d.timeoutSeconds || 30;
    $("searchProviderFreshness").value = d.freshness || "";
    $("searchProviderApiKey").value = "";
  });
}

function searchPlatformConfigJson() {
  return JSON.stringify({
    endpointPath: $("searchProviderEndpointPath").value,
    defaultCount: $("searchProviderDefaultCount").value ? Number($("searchProviderDefaultCount").value) : 10,
    timeoutSeconds: $("searchProviderTimeoutSeconds").value ? Number($("searchProviderTimeoutSeconds").value) : 30,
    freshness: $("searchProviderFreshness").value
  });
}

function searchPlatformCredentialJson() {
  var apiKey = $("searchProviderApiKey").value;
  return apiKey && apiKey.trim() ? JSON.stringify({ apiKey: apiKey }) : "";
}

function renderAiPlatforms() {
  return Promise.all([api('/admin/ai-platforms'), api('/admin/user-ai?page=0&size=20'), loadApps()]).then(function (res) {
    var platforms = res[0] || [];
    var keys = pageContent(res[1]);
    var apps = res[2] || [];
    var currentApp = queryFilters('aiPlatforms').appId || (apps[0] && apps[0].appId) || '';
    setFilters('aiPlatforms', { appId: currentApp });
    $('aiConfigs').innerHTML = panelTitleActions('AI平台列表', '<button type="button" onclick="openAiPlatformCreate()">新建平台</button>') +
      table([
        { title: '编码', key: 'providerCode' },
        { title: '名称', key: 'displayName' },
        { title: '余额', render: function (r) { return '<span id="aiBalance' + r.id + '">-</span>'; } },
        { title: '状态', render: function (r) { return badge(r.enabled ? 'ENABLED' : 'DISABLED'); } },
        { title: '操作', render: function (r) {
          var buttons = '<button class="small" onclick="openAiPlatformDetail(' + r.id + ')">详情</button>';
          buttons += '<button class="small" onclick="openAiPlatformEdit(' + r.id + ')">配置</button>';
          buttons += '<button class="small" onclick="toggleAiPlatform(' + r.id + ', ' + (r.enabled ? "true" : "false") + ')">启停</button>';
          if (isMoacodeProviderCode(r.providerCode)) {
            buttons += '<button class="small secondary" onclick="openMoacodePricing(' + r.id + ')">价格</button>';
            buttons += '<button class="small secondary" onclick="openMoacodeUsage(' + r.id + ')">消耗</button>';
          }
          buttons += '<button class="small danger" onclick="deleteAiPlatform(' + r.id + ')">删除</button>';
          return '<div class="actions">' + buttons + '</div>';
        } }
      ], platforms) +
      '<div style="height:12px"></div>' +
      panel('APP 平台参数', '<div class="toolbar">' +
        select('aiAppFilter', 'APP', apps.map(function (a) { return { value: a.appId, label: a.appId + ' / ' + a.appName }; }), currentApp) +
        '<button class="secondary" type="button" onclick="openAppAiProviderSetting()">配置APP平台参数</button>' +
      '</div>') +
      '<div style="height:12px"></div>' +
      panelTitleActions('用户AI Key / 配额', '<button type="button" onclick="openUserAiUpsert()">配置用户Key</button>') +
      table([
        { title: '用户ID', key: 'userId' },
        { title: 'APP', key: 'appId' },
        { title: '平台', key: 'providerCode' },
        { title: 'Key', render: function (r) { return r.apiKey ? '已配置' : '未配置'; } },
        { title: '配额', key: 'quotaUnits' }
      ], keys) + '</div>';
    loadAiPlatformBalances(platforms);
  });
}

function loadAiPlatformBalances(platforms) {
  (platforms || []).forEach(function (item) {
    var cell = $("aiBalance" + item.id);
    if (!cell) return;
    if (!aiProviderSupportsBalance(item.providerCode)) {
      cell.textContent = "-";
      return;
    }
    cell.textContent = "查询中";
    api('/admin/ai-platforms/' + item.id + '/account-balance').then(function (data) {
      var target = $("aiBalance" + item.id);
      if (target) target.innerHTML = aiBalanceHtml(data, item.providerCode);
    }).catch(function (err) {
      var target = $("aiBalance" + item.id);
      if (target) target.innerHTML = '<span class="muted" title="' + escapeAttr(err.message || "查询失败") + '">查询失败</span>';
    });
  });
}

function aiProviderSupportsBalance(providerCode) {
  var code = String(providerCode || "").toLowerCase();
  return code === "deepseek" || code === "moacode" || code === "moacode-team";
}

function aiBalanceHtml(data, providerCode) {
  data = data || {};
  var code = String(providerCode || data.providerCode || "").toLowerCase();
  var balance = data.balanceSummary || {};
  var usage = data.usageSummary || {};
  var available = firstPresent(data.availableBalance, balance.effectiveAvailableBalance, balance.totalBalance, balance.balance, balance.subscriptionBalance, balance.payAsYouGoBalance);
  var titleParts = [];
  if (code === "deepseek") {
    titleParts.push("总余额 " + formatCurrency(available, "￥", 2));
    var deepseekDetail = [];
    (balance.balanceInfos || []).forEach(function (row) {
      titleParts.push((row.currency || row.currency_code || "CNY") + " total=" + firstPresent(row.total_balance, row.totalBalance, "-") + " granted=" + firstPresent(row.granted_balance, row.grantedBalance, "-") + " topped=" + firstPresent(row.topped_up_balance, row.toppedUpBalance, "-"));
      deepseekDetail.push((row.currency || row.currency_code || "CNY") + " " + formatCurrency(firstPresent(row.total_balance, row.totalBalance), "￥", 2));
    });
    return '<div title="' + escapeAttr(titleParts.join("\n")) + '"><strong>' + escapeHtml(formatCurrency(available, "￥", 2)) + '</strong><div class="muted">' + escapeHtml(deepseekDetail.join(" / ") || "DeepSeek") + '</div></div>';
  }
  if (code === "moacode-team") {
    titleParts.push("有效可用 " + formatCurrency(available, "$", 2));
    titleParts.push("团队 " + firstPresent(balance.teamName, "-"));
    titleParts.push("团队日剩余 " + formatCurrency(balance.teamDailyRemainingBalance, "$", 2));
    titleParts.push("用户日剩余 " + formatCurrency(balance.userDailyRemainingBalance, "$", 2));
    titleParts.push("本月消耗 " + formatCurrency(firstPresent(balance.teamMonthSpend, usage.totalCost), "$", 2));
    return '<div title="' + escapeAttr(titleParts.join("\n")) + '"><strong>' + escapeHtml(formatCurrency(available, "$", 2)) + '</strong><div class="muted">日剩 ' + escapeHtml(formatCurrency(firstPresent(balance.teamDailyRemainingBalance, balance.userDailyRemainingBalance), "$", 2)) + ' / 月耗 ' + escapeHtml(formatCurrency(firstPresent(balance.teamMonthSpend, usage.totalCost), "$", 2)) + '</div></div>';
  }
  titleParts.push("总余额 " + formatCurrency(available, "$", 2));
  titleParts.push("订阅 " + formatCurrency(balance.subscriptionBalance, "$", 2));
  titleParts.push("按量 " + formatCurrency(balance.payAsYouGoBalance, "$", 2));
  titleParts.push("本月成本 " + formatCurrency(usage.totalCost, "$", 2));
  titleParts.push("请求 " + firstPresent(usage.totalRequests, "-"));
  return '<div title="' + escapeAttr(titleParts.join("\n")) + '"><strong>' + escapeHtml(formatCurrency(available, "$", 2)) + '</strong><div class="muted">订阅 ' + escapeHtml(formatCurrency(balance.subscriptionBalance, "$", 2)) + ' / 按量 ' + escapeHtml(formatCurrency(balance.payAsYouGoBalance, "$", 2)) + '</div></div>';
}

function firstPresent() {
  for (var i = 0; i < arguments.length; i += 1) {
    var value = arguments[i];
    if (value !== null && value !== undefined && value !== "") return value;
  }
  return null;
}

function formatCurrency(value, symbol, digits) {
  if (value === null || value === undefined || value === "") return "-";
  var parsed = Number(value);
  if (!Number.isFinite(parsed)) return "-";
  return symbol + parsed.toLocaleString("zh-CN", { maximumFractionDigits: digits, minimumFractionDigits: 0 });
}

function aiPlatformForm(item) {
  item = item || {};
  var config = parseJsonObject(item.configJson || '{}');
  var credential = parseJsonObject(item.credentialJson || '{}');
  var isCreate = !item.id;
  var defaultCode = item.providerCode || 'api2d';
  var defaults = aiPlatformDefaults[defaultCode] || aiPlatformDefaults.api2d;
  var isApi2d = defaultCode === "api2d";
  var isMoacode = isMoacodeProviderCode(defaultCode);
  var singleKey = aiProviderUsesSingleKey(defaultCode);
  var sharedApiKey = credential.adminApiKey || credential.modelApiKey || credential.apiKey || '';
  var pricing = config.tokenPricing || {};
  var defaultPricing = (pricing.models && pricing.models.default) || {};
  return '<div class="form-grid">' +
    (isCreate
      ? select('aiProviderCode', '支持的平台', Object.keys(aiPlatformDefaults).map(optionOf), defaultCode)
      : input('aiProviderCode', '平台编码', defaultCode)) +
    input('aiProviderName', '名称', item.displayName || defaults.displayName) +
    input('aiPublicSortOrder', '排序', config.publicSortOrder || 100, { required: false, type: 'number' }) +
    checkbox('aiPublicHidden', '用户侧隐藏', !!config.publicHidden) +
    input('aiPublicId', '用户侧公开 ID', config.publicId || defaults.publicId || '') +
    input('aiPublicName', '用户侧显示名', config.publicName || defaults.publicName || '') +
    input('aiPublicFamily', '用户侧分组名', config.publicFamily || defaults.publicFamily || '') +
    input('aiDefaultModel', '默认模型', config.defaultModel || defaults.defaultModel || '') +
    input('aiModelBaseUrl', 'AI 调用 Base URL', item.baseUrl || defaults.baseUrl) +
    (isApi2d || isMoacode ? input('aiConsoleBaseUrl', isMoacode ? '余额/用量 Base URL' : '管理 API Base URL', item.consoleBaseUrl || defaults.consoleBaseUrl, { required: false }) : input('aiConsoleBaseUrl', '管理 API Base URL', item.consoleBaseUrl || defaults.consoleBaseUrl, { required: false })) +
    (singleKey ? textarea('aiSharedApiKey', isMoacode ? 'MoaCode API Key' : '平台 API Key', sharedApiKey, { required: false }) :
      textarea('aiAdminApiKey', 'API2D 主账号管理 Token', credential.adminApiKey || '', { required: false }) +
      textarea('aiModelApiKey', '大模型 API Key', credential.modelApiKey || '', { required: false })) +
    (isApi2d || isMoacode ? textarea('aiUsageCookie', isMoacode ? 'MoaCode Cookie' : 'API2D ForwardKey', credential.usageCookie || credential.forwardKey || credential.cookie || '', { required: false }) : '') +
    (isApi2d ? sectionTitle('Key 分配参数') +
      input('aiDefaultKeyTypeId', '默认 Key 分组 ID', config.defaultKeyTypeId || '', { required: false }) +
      input('aiQuotaTransferPath', '额度转入路径', config.quotaTransferPath || '', { required: false }) +
      select('aiQuotaTransferMethod', '额度转入方法', [{ value: 'POST', label: 'POST' }, { value: 'PUT', label: 'PUT' }], config.quotaTransferMethod || 'POST') : '') +
    (!isApi2d ? sectionTitle('价格配置') +
      input('aiPointValueCny', '1P折合人民币', pricing.pointValueCny || '0.01', { required: false, type: 'number' }) +
      input('aiBillingMultiplier', '计费倍率', pricing.billingMultiplier || '1.5', { required: false, type: 'number' }) +
      input('aiPromptPrice', '输入价/百万token', defaultPricing.prompt || '1', { required: false, type: 'number' }) +
      input('aiCompletionPrice', '输出价/百万token', defaultPricing.completion || '2', { required: false, type: 'number' }) +
      input('aiCacheHitPrice', '缓存读价/百万token', defaultPricing.cacheHit || '0.1', { required: false, type: 'number' }) +
      input('aiCacheMissPrice', '缓存写价/百万token', defaultPricing.cacheMiss || '1', { required: false, type: 'number' }) : '') +
    input('aiDocsUrl', '文档地址', config.docs || defaults.docs || '', { required: false }) +
    '</div>';
}

function aiPlatformBody() {
  var provider = $('aiProviderCode').value;
  var singleKey = aiProviderUsesSingleKey(provider);
  var sharedApiKey = singleKey && $('aiSharedApiKey') ? $('aiSharedApiKey').value : '';
  return {
    providerCode: provider,
    displayName: $('aiProviderName').value,
    baseUrl: $('aiModelBaseUrl').value,
    consoleBaseUrl: $('aiConsoleBaseUrl').value,
    configJson: aiPlatformConfigJson(),
    credentialJson: JSON.stringify(singleKey ? {
      adminApiKey: sharedApiKey,
      modelApiKey: sharedApiKey,
      apiKey: sharedApiKey,
      usageCookie: $('aiUsageCookie') ? $('aiUsageCookie').value : ''
    } : {
      adminApiKey: $('aiAdminApiKey').value,
      modelApiKey: $('aiModelApiKey').value,
      usageCookie: $('aiUsageCookie') ? $('aiUsageCookie').value : ''
    })
  };
}

function bindAiPlatformDefault(mode) {
  var provider = $("aiProviderCode");
  if (!provider) return;
  provider.addEventListener("change", function () {
    if (mode === "create") {
      openModal('AI平台配置', aiPlatformForm({ providerCode: provider.value }), '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveAiPlatformCreate()">保存</button>');
      bindAiPlatformDefault("create");
      return;
    }
    var d = aiPlatformDefaults[provider.value] || {};
    $("aiProviderName").value = d.displayName || labelOf(provider.value);
    $("aiModelBaseUrl").value = d.baseUrl || "";
    if ($("aiConsoleBaseUrl")) $("aiConsoleBaseUrl").value = d.consoleBaseUrl || "";
    if ($("aiPublicId")) $("aiPublicId").value = d.publicId || "";
    $("aiPublicName").value = d.publicName || "";
    $("aiPublicFamily").value = d.publicFamily || "";
    $("aiDefaultModel").value = d.defaultModel || "";
    $("aiDocsUrl").value = d.docs || "";
  });
}

function aiPlatformConfigJson() {
  var provider = $('aiProviderCode').value;
  var defaults = aiPlatformDefaults[provider] || {};
  var config = {
    publicId: $('aiPublicId').value,
    publicName: $('aiPublicName').value,
    publicFamily: $('aiPublicFamily').value,
    defaultModel: $('aiDefaultModel').value,
    publicSortOrder: $('aiPublicSortOrder').value ? Number($('aiPublicSortOrder').value) : 100,
    publicHidden: $('aiPublicHidden').checked,
    authScheme: defaults.authScheme || 'bearer',
    billingMode: defaults.billingMode || 'provider_balance',
    defaultKeyTypeId: $('aiDefaultKeyTypeId') ? $('aiDefaultKeyTypeId').value : '',
    quotaTransferPath: $('aiQuotaTransferPath') ? $('aiQuotaTransferPath').value : '',
    quotaTransferMethod: $('aiQuotaTransferMethod') ? $('aiQuotaTransferMethod').value : 'POST',
    supportsChatCompletions: defaults.supportsChatCompletions !== false,
    supportsStreaming: !!defaults.supportsStreaming,
    supportsImages: !!defaults.supportsImages,
    docs: $('aiDocsUrl').value
  };
  if (!aiProviderUsesApi2dPricing(provider)) {
    var modelPricing = {
      prompt: numberOrFallback($('aiPromptPrice') && $('aiPromptPrice').value, 1),
      completion: numberOrFallback($('aiCompletionPrice') && $('aiCompletionPrice').value, 2),
      cacheHit: numberOrFallback($('aiCacheHitPrice') && $('aiCacheHitPrice').value, 0.1),
      cacheMiss: numberOrFallback($('aiCacheMissPrice') && $('aiCacheMissPrice').value, 1)
    };
    config.tokenPricing = {
      unit: 'point',
      costUnit: isMoacodeProviderCode(provider) ? 'USD' : 'CNY',
      perTokens: 1000000,
      pointValueCny: numberOrFallback($('aiPointValueCny') && $('aiPointValueCny').value, 0.01),
      billingMultiplier: numberOrFallback($('aiBillingMultiplier') && $('aiBillingMultiplier').value, 1.5),
      models: {
        default: modelPricing
      }
    };
    if (provider === 'deepseek') {
      config.tokenPricing.models['deepseek-chat'] = modelPricing;
      config.tokenPricing.models['deepseek-reasoner'] = modelPricing;
    }
  }
  return JSON.stringify(config);
}

function isMoacodeProviderCode(providerCode) {
  var code = String(providerCode || "").toLowerCase();
  return code === "moacode" || code === "moacode-team";
}

function aiProviderUsesSingleKey(providerCode) {
  return String(providerCode || "").toLowerCase() !== "api2d";
}

function aiProviderUsesApi2dPricing(providerCode) {
  return String(providerCode || "").toLowerCase() === "api2d";
}

function numberOrFallback(value, fallback) {
  var parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function openAiPlatformCreate() {
  openModal('AI平台配置', aiPlatformForm({}), '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveAiPlatformCreate()">保存</button>');
  bindAiPlatformDefault("create");
}

function saveAiPlatformCreate() {
  api('/admin/ai-platforms', { method: 'POST', body: aiPlatformBody() }).then(function () { closeModal(); renderAiPlatforms(); }).catch(function (err) { toast(err.message); });
}

function openAiPlatformDetail(id) {
  api('/admin/ai-platforms/' + id).then(function (item) {
    var config = parseJsonObject(item.configJson || '{}');
    var body = detailList({
      '编码': item.providerCode,
      '名称': item.displayName,
      '默认模型': config.defaultModel,
      '模型 Base URL': item.baseUrl,
      '后台 Base URL': item.consoleBaseUrl,
      '计费模式': config.billingMode,
      '状态': item.enabled ? '启用' : '停用'
    }) + sectionBlock('模型价格', '<div id="aiPlatformPricingBlock">加载中</div>');
    var footer = '<button class="secondary" type="button" onclick="openAiPlatformEdit(' + id + ')">配置</button>';
    if (isMoacodeProviderCode(item.providerCode)) {
      footer += '<button class="secondary" type="button" onclick="openMoacodeUsage(' + id + ')">消耗</button>';
    }
    footer += '<button class="secondary" type="button" onclick="closeModal()">关闭</button>';
    openModal('AI平台详情', body, footer);
    loadAiPlatformPricingIntoDetail(item);
  }).catch(function (err) { toast(err.message); });
}

function loadAiPlatformPricingIntoDetail(item) {
  var block = $("aiPlatformPricingBlock");
  if (!block) return;
  if (isMoacodeProviderCode(item.providerCode)) {
    api('/admin/ai-platforms/' + item.id + '/moacode/pricing').then(function (data) {
      var rows = data.models || [];
      block.innerHTML = compactTable([
        { title: '模型', key: 'modelName' },
        { title: '供应商', render: function (r) { return formatValue(r.providerDisplay || r.providerName); } },
        { title: '倍率', key: 'rateMultiplier' },
        { title: '输入', key: 'inputTokenPrice' },
        { title: '输出', key: 'outputTokenPrice' },
        { title: '缓存写', key: 'cacheCreationTokenPrice' },
        { title: '缓存读', key: 'cacheReadTokenPrice' },
        { title: '请求', key: 'requestPrice' }
      ], rows);
    }).catch(function (err) {
      block.innerHTML = '<div class="empty-cell">' + escapeHtml(err.message || "价格查询失败") + '</div>';
    });
    return;
  }
  var config = parseJsonObject(item.configJson || '{}');
  var pricing = config.tokenPricing || config.pricing || {};
  var models = pricing.models || {};
  var rows = Object.keys(models).map(function (model) {
    var row = models[model] || {};
    return {
      model: model,
      prompt: row.prompt,
      completion: row.completion,
      cacheHit: row.cacheHit,
      cacheMiss: row.cacheMiss,
      requestCost: row.requestCost
    };
  });
  if (!rows.length && config.defaultModel) {
    rows = [{ model: config.defaultModel, prompt: '-', completion: '-', cacheHit: '-', cacheMiss: '-', requestCost: '-' }];
  }
  block.innerHTML = (pricing.unit || pricing.costUnit || pricing.billingMultiplier || pricing.pointValueCny
    ? detailList({
      '单位': pricing.unit,
      '成本币种': pricing.costUnit,
      '每单位 tokens': pricing.perTokens,
      '1P折合': pricing.pointValueCny,
      '计费倍率': pricing.billingMultiplier
    }) : '') + compactTable([
      { title: '模型', key: 'model' },
      { title: '输入', key: 'prompt' },
      { title: '输出', key: 'completion' },
      { title: '缓存读', key: 'cacheHit' },
      { title: '缓存写', key: 'cacheMiss' },
      { title: '请求', key: 'requestCost' }
    ], rows);
}

function openAiPlatformEdit(id) {
  api('/admin/ai-platforms/' + id).then(function (item) {
    var footer = '';
    if (isMoacodeProviderCode(item.providerCode)) {
      footer += '<button class="secondary" type="button" onclick="openMoacodePricing(' + id + ')">价格</button>';
      footer += '<button class="secondary" type="button" onclick="openMoacodeUsage(' + id + ')">消耗</button>';
    }
    footer += '<button class="danger" type="button" onclick="deleteAiPlatform(' + id + ')">删除</button>' +
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="saveAiPlatformEdit(' + id + ')">保存</button>';
    openModal('编辑AI平台', aiPlatformForm(item), footer);
    var code = $('aiProviderCode'); if (code) code.disabled = true;
  }).catch(function (err) { toast(err.message); });
}

function saveAiPlatformEdit(id) {
  api('/admin/ai-platforms/' + id, { method: 'PUT', body: aiPlatformBody() }).then(function () { closeModal(); renderAiPlatforms(); }).catch(function (err) { toast(err.message); });
}

function toggleAiPlatform(id, enabled) {
  api('/admin/ai-platforms/' + id + '/status', { method: 'PATCH', body: { enabled: !enabled } })
    .then(function () { toast('AI平台状态已更新'); renderAiPlatforms(); })
    .catch(function (err) { toast(err.message); });
}

function deleteAiPlatform(id) {
  if (!confirm('确定删除这个AI平台配置？')) return;
  api('/admin/ai-platforms/' + id, { method: 'DELETE' }).then(function () {
    toast('AI平台配置已删除');
    closeModal();
    renderAiPlatforms();
  }).catch(function (err) { toast(err.message); });
}

function openMoacodePricing(id) {
  api('/admin/ai-platforms/' + id + '/moacode/pricing').then(function (data) {
    var rows = data.models || [];
    openModal('MoaCode 价格表',
      compactTable([
        { title: '模型', key: 'modelName' },
        { title: '供应商', render: function (r) { return formatValue(r.providerDisplay || r.providerName); } },
        { title: '倍率', key: 'rateMultiplier' },
        { title: '输入', key: 'inputTokenPrice' },
        { title: '输出', key: 'outputTokenPrice' },
        { title: '缓存写', key: 'cacheCreationTokenPrice' },
        { title: '缓存读', key: 'cacheReadTokenPrice' },
        { title: '请求', key: 'requestPrice' }
      ], rows),
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openMoacodeUsage(id) {
  api('/admin/ai-platforms/' + id + '/moacode/usage').then(function (data) {
    var balance = data.balanceSummary || {};
    var usage = data.usageSummary || {};
    openModal('MoaCode 消耗',
      detailList({
        '余额': balance.balance,
        '总余额': balance.totalBalance,
        '订阅余额': balance.subscriptionBalance,
        '按量余额': balance.payAsYouGoBalance,
        '团队': balance.teamName,
        '团队日剩余额度': balance.teamDailyRemainingBalance,
        '用户日剩余额度': balance.userDailyRemainingBalance,
        '团队周消耗': balance.teamWeekSpend,
        '团队月消耗': balance.teamMonthSpend,
        '有效可用额度': balance.effectiveAvailableBalance,
        '周额度': balance.weeklyLimit,
        '周消耗': balance.weeklySpentBalance,
        '成员数': usage.memberCount,
        '请求数': usage.totalRequests,
        '输入 tokens': usage.totalInputTokens,
        '输出 tokens': usage.totalOutputTokens,
        '缓存写 tokens': usage.totalCacheCreationTokens,
        '缓存读 tokens': usage.totalCacheReadTokens,
        '总成本': usage.totalCost,
        '首次请求': usage.firstRequestAt,
        '最近请求': usage.lastRequestAt
      }) + sectionBlock('模型消耗', compactTable([
        { title: '模型', key: 'model' },
        { title: '请求数', key: 'requests' },
        { title: '输入', key: 'inputTokens' },
        { title: '输出', key: 'outputTokens' },
        { title: '缓存写', key: 'cacheCreationTokens' },
        { title: '缓存读', key: 'cacheReadTokens' },
        { title: '成本', key: 'cost' }
      ], usage.models || [])),
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openUserAiUpsert() {
  loadApps().then(function (apps) {
    openModal('配置用户AI Key', '<div class="form-grid">' +
      input('userAiUserId', '用户 ID', '') +
      select('userAiAppId', 'APP', apps.map(function (a) { return { value: a.appId, label: a.appId + ' / ' + a.appName }; }), apps[0] && apps[0].appId || '') +
      input('userAiProviderCode', 'AI平台', 'api2d') +
      input('userAiQuota', '转入配额', '0', 'number') +
      textarea('userAiKey', '用户 Key', '', { required: false }) +
      '</div>', '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveUserAiUpsert()">保存</button>');
  });
}

function saveUserAiUpsert() {
  api('/admin/user-ai', { method: 'POST', body: {
    userId: $('userAiUserId').value ? Number($('userAiUserId').value) : null,
    appId: $('userAiAppId').value,
    providerCode: $('userAiProviderCode').value,
    apiKey: $('userAiKey').value,
    quotaUnits: $('userAiQuota').value ? Number($('userAiQuota').value) : 0
  }}).then(function () { closeModal(); renderAiPlatforms(); }).catch(function (err) { toast(err.message); });
}

function openAppAiProviderSetting() {
  loadApps().then(function (apps) {
    var currentApp = $('aiAppFilter') ? $('aiAppFilter').value : (apps[0] && apps[0].appId) || '';
    openModal('APP AI 平台参数', '<div class="form-grid">' +
      select('appAiAppId', 'APP', apps.map(function (a) { return { value: a.appId, label: a.appId + ' / ' + a.appName }; }), currentApp) +
      input('appAiProviderCode', '平台编码', 'api2d') +
      checkbox('appAiEnabled', '启用该平台', true) +
      checkbox('appAiAutoProvision', '自动创建用户Key', true) +
      input('appAiGroupId', 'Key分组ID', '', { required: false }) +
      input('appAiDefaultQuota', '默认配额', '0', 'number') +
      input('appAiDailyLimit', '每日上限', '0', 'number') +
      '</div>', '<button class="secondary" type="button" onclick="closeModal()">取消</button><button type="button" onclick="saveAppAiProviderSetting()">保存</button>');
  });
}

function saveAppAiProviderSetting() {
  api('/admin/app-ai-providers', { method: 'POST', body: {
    appId: $('appAiAppId').value,
    providerCode: $('appAiProviderCode').value,
    enabled: $('appAiEnabled').checked,
    autoProvisionUserKey: $('appAiAutoProvision').checked,
    defaultQuotaUnits: $('appAiDefaultQuota').value ? Number($('appAiDefaultQuota').value) : 0,
    dailyLimitUnits: $('appAiDailyLimit').value ? Number($('appAiDailyLimit').value) : 0,
    keyGroupId: $('appAiGroupId').value
  }}).then(function () { closeModal(); renderAiPlatforms(); }).catch(function (err) { toast(err.message); });
}

function renderTools() {
  var launchCurl = "curl -X POST http://localhost:8888/api/device/launch -H 'Content-Type: application/json' -d '{\"appId\":\"<appId>\",\"deviceCode\":\"<deviceCode>\",\"platform\":\"ios\",\"version\":\"1.0.0\"}'";
  var payCurl = "curl -X POST http://localhost:8888/api/payment/create-order -H 'Content-Type: application/json' -d '{\"appId\":\"<appId>\",\"deviceId\":1,\"packageId\":1,\"payChannel\":\"ALIPAY\"}'";
  $("tools").innerHTML =
    panel("演示数据", '<button type="button" onclick="createDemo()">创建演示数据</button>') +
    '<div style="height:12px"></div>' +
    panel("设备码支付接入", '<div class="actions"><button type="button" onclick="copyEncodedText(\'' + encodeURIComponent(launchCurl) + '\')">复制启动上报 curl</button>' +
      '<button type="button" onclick="copyEncodedText(\'' + encodeURIComponent(payCurl) + '\')">复制支付宝下单 curl</button>' +
      '<button class="secondary" type="button" onclick="downloadOpenApi()">下载 OpenAPI</button></div>') +
    '<div style="height:12px"></div>' +
    panel("模拟支付回调", '<div class="form-grid">' +
      select("mockNotifyChannel", "渠道", [
        optionOf("ALIPAY"),
        optionOf("WECHAT"),
        optionOf("AGGREGATE")
      ], "ALIPAY") +
      input("mockNotifyOrderNo", "订单号") +
      input("mockNotifyTradeNo", "交易号", "MOCK-" + Date.now()) +
      '<button type="button" onclick="mockPaymentNotify()">提交回调</button>' +
      '</div>') +
    '<div style="height:12px"></div>' +
    panel("常用入口", '<p><a href="/swagger" target="_blank">Swagger 接口文档</a></p><p><a href="/docs" target="_blank">文档别名</a></p><p><a href="/actuator/health" target="_blank">健康检查</a></p>');
}

function downloadOpenApi() {
  exportCsv("/v3/api-docs", "lianpayhub-openapi.json");
}

function createDemo() {
  api("/admin/demo/device-vip", { method: "POST" }).then(function (data) {
    toast(data.message);
    switchView("dashboard");
  }).catch(function (err) { toast(err.message); });
}

function mockPaymentNotify() {
  var orderNo = $("mockNotifyOrderNo").value;
  var tradeNo = $("mockNotifyTradeNo").value || ("MOCK-" + Date.now());
  var payload = "verified=true&orderNo=" + encodeURIComponent(orderNo) +
    "&tradeNo=" + encodeURIComponent(tradeNo) +
    "&status=SUCCESS";
  api("/api/payment/notify/" + $("mockNotifyChannel").value, {
    method: "POST",
    body: { payload: payload }
  }).then(function (data) {
    toast(data.message || "回调已处理");
    switchView("callbacks");
  }).catch(function (err) { toast(err.message); });
}

function detailList(map) {
  var html = '<div class="detail-list">';
  var key;
  for (key in map) {
    html += '<div class="detail-item"><div class="k">' + escapeHtml(key) + '</div><div class="v">' + formatValue(map[key]) + '</div></div>';
  }
  html += "</div>";
  return html;
}

function statsGrid(stats) {
  stats = stats || {};
  return '<div class="metric-grid compact">' +
    metric("绑定APP", stats.bindingCount || 0) +
    metric("文件数", stats.fileCount || 0) +
    metric("空间", formatBytes(stats.usedBytes || 0)) +
    metric("套餐", stats.packageCount || 0) +
    metric("设备", stats.deviceCount || 0) +
    metric("会员", stats.memberCount || 0) +
    metric("订单", stats.orderCount || 0) +
    metric("已支付", stats.paidOrderCount || 0) +
    metric("收入", formatMoney(stats.paidAmountCents || 0)) +
    metric("启动", stats.launchCount || 0) +
    metric("登录", stats.loginCount || 0) +
    '</div>';
}

function sectionBlock(title, body) {
  return '<div class="subsection"><h4>' + escapeHtml(title) + '</h4>' + body + '</div>';
}

function compactTable(columns, rows) {
  rows = rows || [];
  return table(columns, rows.slice(0, 10));
}

function copyText(text) {
  if (!navigator.clipboard) {
    toast("当前浏览器不支持复制");
    return;
  }
  navigator.clipboard.writeText(String(text || "")).then(function () { toast("已复制"); })
    .catch(function (err) { toast(err.message); });
}

function copyEncodedText(text) {
  copyText(decodeURIComponent(text || ""));
}

function findById(rows, id) {
  var i;
  for (i = 0; i < rows.length; i++) {
    if (Number(rows[i].id) === Number(id)) return rows[i];
  }
  return null;
}

init();
