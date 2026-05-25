var state = {
  token: localStorage.getItem("lph_token") || "",
  view: "dashboard",
  pageByView: {},
  filtersByView: {},
  logTab: "admin-operations"
};

var titles = {
  dashboard: ["总览", "运营数据与接口状态"],
  apps: ["APP 管理", "创建、编辑、启停与密钥重置"],
  paymentConfigs: ["支付配置", "维护各 APP 的支付渠道和商户参数"],
  packages: ["套餐管理", "维护会员套餐和上下架状态"],
  users: ["用户管理", "统一账号和状态管理"],
  bindings: ["绑定管理", "用户与 APP 的绑定关系"],
  devices: ["设备管理", "设备码、绑定和最近启动记录"],
  members: ["会员管理", "查询、赠送和取消会员"],
  orders: ["订单管理", "订单详情、标记支付与退款入口"],
  refunds: ["退款管理", "申请退款和手动确认"],
  callbacks: ["回调日志", "支付渠道回调验签与处理记录"],
  launches: ["启动记录", "APP 启动、登录与支付事件记录"],
  adapter: ["适配上报", "第三方 APP 运行状态与事件上报"],
  logs: ["日志审计", "后台、登录、启动和支付事件日志"],
  admins: ["管理员", "后台账号、状态和密码管理"],
  tools: ["调试工具", "演示数据和常用入口"]
};

function $(id) { return document.getElementById(id); }

function init() {
  $("loginForm").addEventListener("submit", login);
  $("logoutBtn").addEventListener("click", logout);
  $("refreshBtn").addEventListener("click", renderCurrent);
  $("modalCloseBtn").addEventListener("click", closeModal);
  $("modalMask").addEventListener("click", function (e) {
    if (e.target === $("modalMask")) closeModal();
  });
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") closeModal();
  });
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    btn.addEventListener("click", function () { switchView(btn.dataset.view); });
  });
  if (state.token) showApp(); else showLogin();
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
  var headers = {};
  if (state.token) headers["Authorization"] = "Bearer " + state.token;
  fetch(path, { headers: headers }).then(function (res) {
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
  return escapeHtml(value);
}

function formatMoney(cents) {
  if (cents === null || cents === undefined || cents === "") return "-";
  return (Number(cents) / 100).toFixed(2);
}

function badge(value) {
  var text = escapeHtml(value);
  var cls = "badge";
  if (["ENABLED", "ACTIVE", "PAID", "SUCCESS"].indexOf(value) >= 0) cls += " ok";
  if (["PENDING", "PARTIAL_REFUNDED", "RECEIVED"].indexOf(value) >= 0) cls += " warn";
  if (["DISABLED", "CANCELLED", "FAILED", "REFUNDED"].indexOf(value) >= 0) cls += " bad";
  return '<span class="' + cls + '">' + text + '</span>';
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
  var head = columns.map(function (c) { return "<th>" + c.title + "</th>"; }).join("");
  var body = rows.map(function (row) {
    return "<tr>" + columns.map(function (c) {
      var val = typeof c.render === "function" ? c.render(row) : formatValue(row[c.key]);
      return "<td>" + val + "</td>";
    }).join("") + "</tr>";
  }).join("");
  return "<table><thead><tr>" + head + "</tr></thead><tbody>" +
    (body || '<tr><td colspan="' + columns.length + '">暂无数据</td></tr>') +
    "</tbody></table>";
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
  return '<div class="panel ' + (extraClass || "") + '"><div class="panel-title">' + title + '</div>' + body + '</div>';
}

function input(id, label, value, type) {
  return '<label>' + label + '<input id="' + id + '" type="' + (type || "text") + '" value="' + escapeHtml(value || "") + '"></label>';
}

function textarea(id, label, value) {
  return '<label>' + label + '<textarea id="' + id + '">' + escapeHtml(value || "") + '</textarea></label>';
}

function select(id, label, values, value) {
  var html = '<label>' + label + '<select id="' + id + '">';
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
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    btn.classList.toggle("active", btn.dataset.view === view);
  });
  Array.prototype.forEach.call(document.querySelectorAll(".view"), function (el) {
    el.classList.toggle("hidden", el.id !== view);
  });
  $("pageTitle").textContent = titles[view][0];
  $("subTitle").textContent = titles[view][1];
  renderCurrent();
}

function renderCurrent() {
  var map = {
    dashboard: renderDashboard,
    apps: renderApps,
    paymentConfigs: renderPaymentConfigs,
    packages: renderPackages,
    users: renderUsers,
    bindings: renderBindings,
    devices: renderDevices,
    members: renderMembers,
    orders: renderOrders,
    refunds: renderRefunds,
    callbacks: renderCallbacks,
    launches: renderLaunches,
    adapter: renderAdapterReports,
    logs: renderLogs,
    admins: renderAdmins,
    tools: renderTools
  };
  map[state.view]().catch(function (err) { toast(err.message); });
}

function openModal(title, body, footer) {
  $("modalTitle").textContent = title;
  $("modalBody").innerHTML = body;
  $("modalFooter").innerHTML = footer || "";
  $("modalMask").classList.remove("hidden");
}

function closeModal() {
  $("modalMask").classList.add("hidden");
  $("modalBody").innerHTML = "";
  $("modalFooter").innerHTML = "";
}

function renderDashboard() {
  return Promise.all([
    api("/admin/reports/overview"),
    api("/admin/reports/trend?days=14"),
    api("/admin/reports/payment-summary")
  ]).then(function (res) {
    var overview = res[0], trend = res[1], summary = res[2] || {};
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
      panel("近 14 天趋势", table([
        { title: "日期", key: "date" },
        { title: "订单", key: "orderCount" },
        { title: "支付订单", key: "paidOrderCount" },
        { title: "支付金额(分)", key: "paidAmountCents" },
        { title: "登录", key: "loginCount" },
        { title: "启动", key: "launchCount" }
      ], trend)) +
      '<div style="height:12px"></div>' +
      panel("APP 收入排行", table([
        { title: "APP", key: "dimension" },
        { title: "订单", key: "orderCount" },
        { title: "支付订单", key: "paidOrderCount" },
        { title: "支付金额(元)", render: function (r) { return formatMoney(r.paidAmountCents); } }
      ], summary.byApp || [])) +
      '<div style="height:12px"></div>' +
      panel("支付渠道分布", table([
        { title: "渠道", key: "dimension" },
        { title: "订单", key: "orderCount" },
        { title: "支付订单", key: "paidOrderCount" },
        { title: "支付金额(元)", render: function (r) { return formatMoney(r.paidAmountCents); } }
      ], summary.byPayChannel || []));
  });
}

function metric(label, value) {
  return '<div class="metric"><div class="label">' + label + '</div><div class="value">' + formatValue(value) + '</div></div>';
}

function loadApps() {
  return api("/admin/apps").then(function (rows) {
    return rows || [];
  });
}

function renderApps() {
  return loadApps().then(function (rows) {
    var createBody = '<div class="form-grid">' +
      input("appId", "APP ID") +
      input("appName", "APP 名称") +
      select("appType", "类型", [
        { value: "STANDARD", label: "STANDARD" },
        { value: "DEVICE_ONLY", label: "DEVICE_ONLY" },
        { value: "ADAPTER", label: "ADAPTER" }
      ], "STANDARD") +
      checkbox("needMobileLogin", "手机号登录", true) +
      checkbox("needDeviceVip", "设备会员", false) +
      '<button type="button" onclick="createApp()">创建</button>' +
      '<button class="secondary" type="button" onclick="exportCsv(\'/admin/exports/apps?limit=5000\', \'apps.csv\')">导出</button>' +
      "</div>";

    $("apps").innerHTML =
      panel("创建 APP", createBody) +
      '<div style="height:12px"></div>' +
      panel("APP 列表", table([
        { title: "ID", key: "id" },
        { title: "APP ID", key: "appId" },
        { title: "名称", key: "appName" },
        { title: "类型", key: "appType" },
        { title: "密钥版本", key: "appSecretVersion" },
        { title: "手机号登录", render: function (r) { return badge(r.needMobileLogin ? "ENABLED" : "DISABLED"); } },
        { title: "设备会员", render: function (r) { return badge(r.needDeviceVip ? "ENABLED" : "DISABLED"); } },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openAppDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="openAppEdit(' + r.id + ')">编辑</button>' +
              '<button class="small" onclick="toggleApp(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '<button class="small" onclick="resetSecret(' + r.id + ')">重置密钥</button>' +
              '</div>';
          }
        }
      ], rows));
  });
}

function createApp() {
  api("/admin/apps", {
    method: "POST",
    body: {
      appId: $("appId").value,
      appName: $("appName").value,
      appType: $("appType").value,
      needMobileLogin: $("needMobileLogin").checked,
      needDeviceVip: $("needDeviceVip").checked
    }
  }).then(function (data) {
    toast("创建成功，secret: " + data.appSecret);
    renderApps();
  }).catch(function (err) { toast(err.message); });
}

function openAppDetail(id) {
  api("/admin/apps").then(function (rows) {
    var item = findById(rows, id);
    if (!item) throw new Error("APP 不存在");
    openModal("APP 详情", detailList({
      "ID": item.id,
      "APP ID": item.appId,
      "名称": item.appName,
      "类型": item.appType,
      "密钥版本": item.appSecretVersion,
      "手机号登录": item.needMobileLogin,
      "设备会员": item.needDeviceVip,
      "状态": item.status,
      "密钥哈希": item.appSecretHash || "-"
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openAppEdit(id) {
  api("/admin/apps").then(function (rows) {
    var item = findById(rows, id);
    if (!item) throw new Error("APP 不存在");
    openModal("编辑 APP", '<div class="form-grid">' +
      input("editAppName", "APP 名称", item.appName) +
      checkbox("editNeedMobileLogin", "手机号登录", !!item.needMobileLogin) +
      checkbox("editNeedDeviceVip", "设备会员", !!item.needDeviceVip) +
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
      needDeviceVip: $("editNeedDeviceVip").checked
    }
  }).then(function () {
    toast("APP 已更新");
    closeModal();
    renderApps();
  }).catch(function (err) { toast(err.message); });
}

function toggleApp(id, status) {
  api("/admin/apps/" + id + "/status", {
    method: "PATCH",
    body: { status: status === "ENABLED" ? "DISABLED" : "ENABLED" }
  }).then(function () {
    toast("状态已更新");
    renderApps();
  }).catch(function (err) { toast(err.message); });
}

function resetSecret(id) {
  api("/admin/apps/" + id + "/reset-secret", { method: "POST" }).then(function (data) {
    toast("新 secret: " + data.appSecret);
  }).catch(function (err) { toast(err.message); });
}

function paymentChannelOptions(includeAll) {
  var values = includeAll ? [{ value: "", label: "全部渠道" }] : [];
  return values.concat([
    { value: "ALIPAY", label: "ALIPAY" },
    { value: "WECHAT", label: "WECHAT" },
    { value: "AGGREGATE", label: "AGGREGATE" }
  ]);
}

function paymentConfigStatusOptions(includeAll) {
  var values = includeAll ? [{ value: "", label: "全部状态" }] : [];
  return values.concat([
    { value: "ENABLED", label: "ENABLED" },
    { value: "DISABLED", label: "DISABLED" }
  ]);
}

function renderPaymentConfigs(page) {
  if (typeof page === "number") setPage("paymentConfigs", page);
  page = currentPage("paymentConfigs");
  return loadApps().then(function (apps) {
    var filters = queryFilters("paymentConfigs");
    setFilters("paymentConfigs", {
      appId: filters.appId || "",
      payChannel: filters.payChannel || "",
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
    if (filters.payChannel) qs.push("payChannel=" + encodeURIComponent(filters.payChannel));
    if (filters.status) qs.push("status=" + encodeURIComponent(filters.status));
    return api("/admin/payment-configs?" + qs.join("&")).then(function (data) {
      var rows = pageContent(data);
      var createBody = '<div class="form-grid">' +
        select("payCfgCreateAppId", "APP", createAppOptions, filters.appId || (apps[0] && apps[0].appId) || "") +
        select("payCfgCreateChannel", "支付渠道", paymentChannelOptions(false), "AGGREGATE") +
        input("payCfgCreateProvider", "提供方编码", "aggregate") +
        input("payCfgCreateMerchant", "商户号") +
        input("payCfgCreateChannelApp", "渠道 APP ID") +
        input("payCfgCreateNotify", "回调地址") +
        textarea("payCfgCreateConfig", "普通配置 JSON", "{}") +
        textarea("payCfgCreateCredential", "敏感凭据 JSON") +
        '<button type="button" onclick="createPaymentConfig()">创建配置</button>' +
        '</div>';
      var filterBar = '<div class="toolbar">' +
        select("payCfgAppFilter", "APP", appOptions, filters.appId || "") +
        select("payCfgChannelFilter", "支付渠道", paymentChannelOptions(true), filters.payChannel || "") +
        select("payCfgStatusFilter", "状态", paymentConfigStatusOptions(true), filters.status || "") +
        '<button class="secondary" type="button" onclick="applyPaymentConfigFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportPaymentConfigs()">导出</button>' +
        '</div>';
      $("paymentConfigs").innerHTML =
        panel("创建支付配置", createBody) +
        '<div style="height:12px"></div>' +
        panel("筛选", filterBar) +
        '<div style="height:12px"></div>' +
        panel("配置列表", table([
          { title: "ID", key: "id" },
          { title: "APP", key: "appId" },
          { title: "渠道", key: "payChannel" },
          { title: "提供方", key: "providerCode" },
          { title: "商户号", key: "merchantId" },
          { title: "凭据", render: function (r) { return r.credentialConfigured ? "已配置" : "未配置"; } },
          { title: "状态", render: function (r) { return badge(r.status); } },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openPaymentConfigDetail(' + r.id + ')">详情</button>' +
                '<button class="small" onclick="openPaymentConfigEdit(' + r.id + ')">编辑</button>' +
                '<button class="small" onclick="togglePaymentConfig(' + r.id + ', \'' + r.status + '\')">启停</button>' +
                '</div>';
            }
          }
        ], rows)) +
        renderPager("paymentConfigs", pageMeta(data), "renderPaymentConfigs");
    });
  });
}

function applyPaymentConfigFilter() {
  setFilters("paymentConfigs", {
    appId: $("payCfgAppFilter").value,
    payChannel: $("payCfgChannelFilter").value,
    status: $("payCfgStatusFilter").value
  });
  renderPaymentConfigs(0);
}

function createPaymentConfig() {
  api("/admin/payment-configs", {
    method: "POST",
    body: {
      appId: $("payCfgCreateAppId").value,
      payChannel: $("payCfgCreateChannel").value,
      providerCode: $("payCfgCreateProvider").value,
      merchantId: $("payCfgCreateMerchant").value,
      channelAppId: $("payCfgCreateChannelApp").value,
      notifyUrl: $("payCfgCreateNotify").value,
      configJson: $("payCfgCreateConfig").value,
      credentialJson: $("payCfgCreateCredential").value
    }
  }).then(function () {
    toast("支付配置已创建");
    renderPaymentConfigs(0);
  }).catch(function (err) { toast(err.message); });
}

function openPaymentConfigDetail(id) {
  api("/admin/payment-configs/" + id).then(function (item) {
    openModal("支付配置详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "渠道": item.payChannel,
      "提供方": item.providerCode,
      "商户号": item.merchantId,
      "渠道 APP ID": item.channelAppId,
      "回调地址": item.notifyUrl,
      "普通配置": item.configJson,
      "敏感凭据": item.credentialConfigured ? "已配置" : "未配置",
      "状态": item.status,
      "创建时间": item.createdAt,
      "更新时间": item.updatedAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openPaymentConfigEdit(id) {
  api("/admin/payment-configs/" + id).then(function (item) {
    openModal("编辑支付配置", '<div class="form-grid">' +
      input("payCfgEditProvider", "提供方编码", item.providerCode) +
      input("payCfgEditMerchant", "商户号", item.merchantId) +
      input("payCfgEditChannelApp", "渠道 APP ID", item.channelAppId) +
      input("payCfgEditNotify", "回调地址", item.notifyUrl) +
      textarea("payCfgEditConfig", "普通配置 JSON", item.configJson || "{}") +
      textarea("payCfgEditCredential", "敏感凭据 JSON（留空不修改）", "") +
      '</div>',
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="savePaymentConfigEdit(' + id + ')">保存</button>');
  }).catch(function (err) { toast(err.message); });
}

function savePaymentConfigEdit(id) {
  api("/admin/payment-configs/" + id, {
    method: "PUT",
    body: {
      providerCode: $("payCfgEditProvider").value,
      merchantId: $("payCfgEditMerchant").value,
      channelAppId: $("payCfgEditChannelApp").value,
      notifyUrl: $("payCfgEditNotify").value,
      configJson: $("payCfgEditConfig").value,
      credentialJson: $("payCfgEditCredential").value
    }
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
          { title: "价格(分)", key: "priceCents" },
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
        { value: "MEMBERSHIP", label: "MEMBERSHIP" },
        { value: "FEATURE", label: "FEATURE" }
      ], "MEMBERSHIP") +
      input("createPkgPrice", "价格(分)", "990") +
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
      priceCents: Number($("createPkgPrice").value),
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
      input("editPkgPrice", "价格(分)", item.priceCents) +
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
      priceCents: Number($("editPkgPrice").value),
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
  setFilters("users", { mobile: filters.mobile || "" });
  var qs = ["page=" + page, "size=20"];
  if (filters.mobile) qs.push("mobile=" + encodeURIComponent(filters.mobile));
  return api("/admin/users?" + qs.join("&")).then(function (data) {
    var rows = pageContent(data);
    var filterBar = '<div class="toolbar">' +
      input("userMobileFilter", "手机号", filters.mobile || "") +
      '<button class="secondary" type="button" onclick="applyUserFilter()">筛选</button>' +
      '<button class="secondary" type="button" onclick="exportUsers()">导出</button>' +
      "</div>";
    $("users").innerHTML =
      panel("筛选", filterBar) + '<div style="height:12px"></div>' +
      panel("用户列表", table([
        { title: "ID", key: "id" },
        { title: "手机号", key: "mobile" },
        { title: "类型", key: "userType" },
        { title: "状态", render: function (r) { return badge(r.status); } },
        {
          title: "操作",
          render: function (r) {
            return '<div class="actions">' +
              '<button class="small" onclick="openUserDetail(' + r.id + ')">详情</button>' +
              '<button class="small" onclick="toggleUser(' + r.id + ', \'' + r.status + '\')">启停</button>' +
              '</div>';
          }
        }
      ], rows)) +
      renderPager("users", pageMeta(data), "renderUsers");
  });
}

function applyUserFilter() {
  setFilters("users", { mobile: $("userMobileFilter").value });
  renderUsers(0);
}

function openUserDetail(id) {
  api("/admin/users?page=0&size=100").then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("用户不存在");
    openModal("用户详情", detailList({
      "ID": item.id,
      "手机号": item.mobile,
      "类型": item.userType,
      "状态": item.status,
      "创建时间": item.createdAt,
      "更新时间": item.updatedAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
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
      var createBody = '<div class="form-grid">' +
        select("bindingCreateAppId", "APP", createAppOptions, filters.appId || (apps[0] && apps[0].appId) || "") +
        input("bindingCreateUserId", "用户 ID") +
        select("bindingCreateType", "绑定类型", [
          { value: "MOBILE_LOGIN", label: "MOBILE_LOGIN" },
          { value: "DEVICE_BIND", label: "DEVICE_BIND" }
        ], "MOBILE_LOGIN") +
        '<button type="button" onclick="createBinding()">创建绑定</button>' +
        '</div>';
      var filterBar = '<div class="toolbar">' +
        select("bindingAppFilter", "APP", appOptions, filters.appId || "") +
        input("bindingUserFilter", "用户 ID", filters.userId || "") +
        select("bindingStatusFilter", "状态", [
          { value: "", label: "全部状态" },
          { value: "ENABLED", label: "ENABLED" },
          { value: "DISABLED", label: "DISABLED" }
        ], filters.status || "") +
        '<button class="secondary" type="button" onclick="applyBindingFilter()">筛选</button>' +
        '<button class="secondary" type="button" onclick="exportBindings()">导出</button>' +
        '</div>';
      $("bindings").innerHTML =
        panel("创建绑定", createBody) +
        '<div style="height:12px"></div>' +
        panel("筛选", filterBar) +
        '<div style="height:12px"></div>' +
        panel("绑定列表", table([
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
        ], rows)) +
        renderPager("bindings", pageMeta(data), "renderBindings");
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
                '<button class="small" onclick="openDeviceBindUser(' + r.id + ')">绑定用户</button>' +
                '<button class="small danger" onclick="unbindDevice(' + r.id + ')">解绑</button>' +
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
  api("/admin/devices?appId=" + encodeURIComponent(queryFilters("devices").appId || "") + "&page=0&size=100").then(function (data) {
    var item = findById(pageContent(data), id);
    if (!item) throw new Error("设备不存在");
    openModal("设备详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "设备码": item.deviceCode,
      "设备名": item.deviceName,
      "设备类型": item.deviceType,
      "设备指纹": item.deviceFingerprint,
      "用户 ID": item.userId,
      "绑定状态": item.bindStatus,
      "绑定时间": item.bindAt,
      "最近启动": item.lastLaunchAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
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
  api("/admin/devices/" + id + "/unbind", { method: "POST" }).then(function () {
    toast("设备已解绑");
    renderDevices(currentPage("devices"));
  }).catch(function (err) { toast(err.message); });
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
        { value: "USER", label: "USER" },
        { value: "DEVICE", label: "DEVICE" }
      ], "USER") +
      input("grantUserId", "用户 ID") +
      input("grantDeviceId", "设备 ID") +
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
  api("/admin/members/" + id + "/cancel", { method: "POST" }).then(function () {
    toast("会员已取消");
    renderMembers(currentPage("members"));
  }).catch(function (err) { toast(err.message); });
}

function renderOrders(page) {
  if (typeof page === "number") setPage("orders", page);
  page = currentPage("orders");
  return loadApps().then(function (apps) {
    var filters = queryFilters("orders");
    var currentApp = filters.appId || (apps[0] && apps[0].appId) || "";
    setFilters("orders", { appId: currentApp });
    return api("/admin/orders?appId=" + encodeURIComponent(currentApp) + "&page=" + page + "&size=20").then(function (data) {
      var rows = pageContent(data);
      var filterBar = '<div class="toolbar">' +
        select("orderAppFilter", "APP", apps.map(function (a) {
          return { value: a.appId, label: a.appId + " / " + a.appName };
        }), currentApp) +
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
          { title: "金额(分)", key: "amountCents" },
          { title: "退款(分)", key: "refundedAmountCents" },
          { title: "渠道", key: "payChannel" },
          { title: "状态", render: function (r) { return badge(r.payStatus); } },
          {
            title: "操作",
            render: function (r) {
              var actions = '<button class="small" onclick="openOrderDetail(' + r.id + ')">详情</button>';
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
  setFilters("orders", { appId: $("orderAppFilter").value });
  renderOrders(0);
}

function openOrderCreate() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    openModal("创建订单", '<div class="form-grid">' +
      select("createOrderAppId", "APP", apps.map(function (a) {
        return { value: a.appId, label: a.appId + " / " + a.appName };
      }), queryFilters("orders").appId || apps[0].appId) +
      input("createOrderUserId", "用户 ID") +
      input("createOrderDeviceId", "设备 ID") +
      input("createOrderPackageId", "套餐 ID") +
      select("createOrderChannel", "支付渠道", [
        { value: "ALIPAY", label: "ALIPAY" },
        { value: "WECHAT", label: "WECHAT" },
        { value: "AGGREGATE", label: "AGGREGATE" }
      ], "AGGREGATE") +
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
      payChannel: $("createOrderChannel").value
    }
  }).then(function (data) {
    toast("订单已创建: " + data.orderNo);
    closeModal();
    renderOrders(0);
  }).catch(function (err) { toast(err.message); });
}

function openOrderDetail(id) {
  api("/admin/orders/" + id).then(function (item) {
    openModal("订单详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "用户": item.userId,
      "设备": item.deviceId,
      "套餐": item.packageId,
      "订单号": item.orderNo,
      "金额(分)": item.amountCents,
      "支付渠道": item.payChannel,
      "支付提供方": item.payProvider,
      "状态": item.payStatus,
      "交易号": item.tradeNo,
      "渠道订单号": item.channelOrderNo,
      "回调次数": item.callbackCount,
      "退款金额(分)": item.refundedAmountCents,
      "支付时间": item.paidAt,
      "过期时间": item.expireAt,
      "关闭时间": item.closedAt,
      "关闭原因": item.closeReason
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function markPaid(id) {
  api("/admin/orders/" + id + "/mark-paid", {
    method: "POST",
    body: { tradeNo: "MANUAL-" + Date.now() }
  }).then(function () {
    toast("已标记支付");
    renderOrders(currentPage("orders"));
  }).catch(function (err) { toast(err.message); });
}

function closeOrder(id) {
  var reason = window.prompt("关闭原因", "后台手动关闭");
  if (reason === null) return;
  api("/admin/orders/" + id + "/close", {
    method: "POST",
    body: { reason: reason }
  }).then(function () {
    toast("订单已关闭");
    renderOrders(currentPage("orders"));
  }).catch(function (err) { toast(err.message); });
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
          { title: "金额(分)", key: "amountCents" },
          { title: "状态", render: function (r) { return badge(r.status); } },
          { title: "原因", key: "reason" },
          {
            title: "操作",
            render: function (r) {
              return '<div class="actions">' +
                '<button class="small" onclick="openRefundDetail(' + r.id + ')">详情</button>' +
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
    input("refundAmount", "金额(分)") +
    textarea("refundReason", "原因", "人工退款") +
    "</div>",
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button type="button" onclick="saveRefundCreate()">申请</button>');
}

function saveRefundCreate() {
  api("/admin/refunds", {
    method: "POST",
    body: {
      orderId: Number($("refundOrderId").value),
      amountCents: Number($("refundAmount").value),
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
      "金额(分)": item.amountCents,
      "原因": item.reason,
      "状态": item.status,
      "渠道退款号": item.channelRefundNo,
      "处理时间": item.processedAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function refundOk(id) {
  api("/admin/refunds/" + id + "/mark-success", {
    method: "POST",
    body: { channelRefundNo: "MANUAL-REFUND-" + Date.now() }
  }).then(function () {
    toast("退款成功");
    renderRefunds(currentPage("refunds"));
  }).catch(function (err) { toast(err.message); });
}

function refundFail(id) {
  api("/admin/refunds/" + id + "/mark-failed", { method: "POST" }).then(function () {
    toast("退款失败已记录");
    renderRefunds(currentPage("refunds"));
  }).catch(function (err) { toast(err.message); });
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
          { title: "渠道", key: "payChannel" },
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
          { title: "版本", key: "version" },
          { title: "网络", key: "networkType" },
          { title: "事件", key: "eventType" },
          { title: "时间", key: "createdAt" },
          { title: "操作", render: function (r) { return '<button class="small" onclick="openLaunchDetail(' + r.id + ')">详情</button>'; } }
        ], rows)) +
        renderPager("launches", pageMeta(data), "renderLaunches");
    });
  });
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
    openModal("启动详情", detailList({
      "ID": item.id,
      "APP": item.appId,
      "设备 ID": item.deviceId,
      "用户 ID": item.userId,
      "平台": item.platform,
      "版本": item.version,
      "网络": item.networkType,
      "IP": item.ipAddress,
      "事件": item.eventType,
      "内容": item.eventData,
      "时间": item.createdAt
    }), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
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
        { title: "金额(分)", key: "amountCents" },
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
      details["金额(分)"] = item.amountCents;
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

function exportPackages() {
  var f = queryFilters("packages");
  exportCsv("/admin/exports/packages" + queryString({ appId: f.appId, limit: 5000 }), "packages.csv");
}

function exportUsers() {
  var f = queryFilters("users");
  exportCsv("/admin/exports/users" + queryString({ mobile: f.mobile, limit: 5000 }), "users.csv");
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
      input("ownNewPassword", "新密码", "", "password") +
      '<button type="button" onclick="changeOwnPassword()">修改密码</button>' +
      '</div>';
    var createBody = '<div class="form-grid">' +
      input("adminUsername", "用户名") +
      input("adminDisplayName", "显示名") +
      input("adminPassword", "初始密码", "", "password") +
      '<button type="button" onclick="createAdminUser()">创建管理员</button>' +
      '</div>';
    var filterBar = '<div class="toolbar">' +
      input("adminUsernameFilter", "用户名", filters.username || "") +
      '<button class="secondary" type="button" onclick="applyAdminFilter()">筛选</button>' +
      '<button class="secondary" type="button" onclick="exportAdmins()">导出</button>' +
      "</div>";
    $("admins").innerHTML =
      panel("修改当前密码", passwordBody) +
      '<div style="height:12px"></div>' +
      panel("创建管理员", createBody) +
      '<div style="height:12px"></div>' +
      panel("筛选", filterBar) +
      '<div style="height:12px"></div>' +
      panel("管理员列表", table([
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
      ], rows)) +
      renderPager("admins", pageMeta(data), "renderAdmins");
  });
}

function applyAdminFilter() {
  setFilters("admins", { username: $("adminUsernameFilter").value });
  renderAdmins(0);
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
    input("resetAdminPassword", "新密码", "", "password") +
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

function renderTools() {
  $("tools").innerHTML =
    panel("演示数据", '<p class="muted">创建演示设备码 APP、套餐、设备、订单，并标记支付成功。</p><button type="button" onclick="createDemo()">创建演示数据</button>') +
    '<div style="height:12px"></div>' +
    panel("模拟支付回调", '<div class="form-grid">' +
      select("mockNotifyChannel", "渠道", [
        { value: "ALIPAY", label: "ALIPAY" },
        { value: "WECHAT", label: "WECHAT" },
        { value: "AGGREGATE", label: "AGGREGATE" }
      ], "AGGREGATE") +
      input("mockNotifyOrderNo", "订单号") +
      input("mockNotifyTradeNo", "交易号", "MOCK-" + Date.now()) +
      '<button type="button" onclick="mockPaymentNotify()">提交回调</button>' +
      '</div>') +
    '<div style="height:12px"></div>' +
    panel("常用入口", '<p><a href="/swagger-ui/index.html" target="_blank">Swagger 接口文档</a></p><p><a href="/actuator/health" target="_blank">健康检查</a></p>');
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

function findById(rows, id) {
  var i;
  for (i = 0; i < rows.length; i++) {
    if (Number(rows[i].id) === Number(id)) return rows[i];
  }
  return null;
}

init();
