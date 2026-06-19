var themeOptions = ["dark", "light"];
var accentOptions = ["teal", "blue", "gold", "rose", "violet"];

var state = {
  token: localStorage.getItem("lph_token") || "",
  view: localStorage.getItem("lph_view") || "dashboard",
  pageByView: {},
  filtersByView: {},
  channelsTab: localStorage.getItem("lph_channels_tab") || "payment",
  commerceTab: localStorage.getItem("lph_commerce_tab") || "packages",
  logTab: "admin-operations",
  theme: localStorage.getItem("lph_theme") || "dark",
  accent: localStorage.getItem("lph_accent") || "teal"
};

var titles = {
  dashboard: ["总览", "运营数据与接口状态"],
  apps: ["APP 管理", "创建、编辑、启停与密钥重置"],
  channels: ["平台配置", "支付、短信与邮件平台配置"],
  commerce: ["交易管理", "套餐、订单与退款集中处理"],
  users: ["用户管理", "统一账号和状态管理"],
  bindings: ["绑定管理", "用户与 APP 的绑定关系"],
  devices: ["设备管理", "设备码、绑定和最近启动记录"],
  members: ["会员管理", "查询、赠送和取消会员"],
  callbacks: ["回调日志", "支付渠道回调验签与处理记录"],
  launches: ["启动记录", "APP 启动、登录与支付事件记录"],
  adapter: ["适配上报", "第三方 APP 运行状态与事件上报"],
  logs: ["日志审计", "后台、登录、启动和支付事件日志"],
  admins: ["管理员", "后台账号、状态和密码管理"],
  tools: ["调试工具", "演示数据和常用入口"]
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
  "aliyun": "阿里云",
  "tencent": "腾讯云",
  "aggregate": "HTTP 聚合平台",
  "local": "本地日志",
  "smtp": "SMTP 邮箱",
  "aliyun-dm": "阿里云邮件推送",
  "tencent-ses": "腾讯云 SES",
  "sendcloud": "SendCloud",
  "mailgun": "Mailgun"
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

var notificationDefaults = {
  SMS: {
    aliyun: {
      displayName: "阿里云短信",
      senderName: "联付中枢",
      senderAddress: "联付中枢",
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
      senderName: "联付中枢",
      senderAddress: "联付中枢",
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
  if (["payment", "sms", "email"].indexOf(state.channelsTab) < 0) state.channelsTab = "payment";
  if (["packages", "orders", "refunds"].indexOf(state.commerceTab) < 0) state.commerceTab = "packages";
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
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") closeModal();
  });
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    btn.addEventListener("click", function () { switchView(btn.dataset.view); });
  });
  if (state.token) showApp(); else showLogin();
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

function badge(value) {
  var text = escapeHtml(labelOf(value));
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
  return '<div class="table-wrap"><table><thead><tr>' + head + "</tr></thead><tbody>" +
    (body || '<tr><td class="empty-cell" colspan="' + columns.length + '">暂无数据</td></tr>') +
    "</tbody></table></div>";
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

function textarea(id, label, value, meta) {
  meta = normalizeFieldMeta(meta);
  return '<label class="' + fieldClass(id, "textarea-field") + '">' + fieldLabel(label, meta) + '<textarea id="' + id + '">' + escapeHtml(value || "") + '</textarea></label>';
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
    scopedInput(prefix + "AccessKeyId", "AccessKey ID", item.accessKeyId, visibility.isAliyun, { required: visibility.isAliyun, conditional: !visibility.isAliyun, hint: visibility.isAliyun ? "阿里云发送必填；也可放到附加凭据 JSON 的 accessKeyId" : "仅阿里云短信需要" }) +
    scopedInput(prefix + "AccessKeySecret", "AccessKey Secret", item.accessKeySecret, visibility.isAliyun, { required: visibility.isAliyun, conditional: !visibility.isAliyun, hint: visibility.isAliyun ? (editing ? "留空不修改；也可放到附加凭据 JSON 的 accessKeySecret" : "阿里云发送必填；也可放到附加凭据 JSON 的 accessKeySecret") : "仅阿里云短信需要", type: "password" }) +
    scopedInput(prefix + "SecretId", "SecretId", item.secretId, visibility.isTencent, { required: visibility.isTencent, conditional: !visibility.isTencent, hint: visibility.isTencent ? "腾讯云发送必填" : "仅腾讯云短信需要" }) +
    scopedInput(prefix + "SecretKey", "SecretKey", item.secretKey, visibility.isTencent, { required: visibility.isTencent, conditional: !visibility.isTencent, hint: visibility.isTencent ? (editing ? "留空不修改" : "腾讯云发送必填") : "仅腾讯云短信需要", type: "password" }) +
    scopedInput(prefix + "SdkAppId", "SDK App ID", item.sdkAppId, visibility.isTencent, { required: visibility.isTencent, conditional: !visibility.isTencent, hint: visibility.isTencent ? "腾讯云发送必填" : "仅腾讯云短信需要" }) +
    scopedInput(prefix + "Region", "地域", item.region, visibility.isTencent, { required: false, hint: "腾讯云常用，其他通道可留空" });
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

function notificationBody(type, prefix) {
  var body = {
    providerCode: $(prefix + "Provider").value,
    displayName: $(prefix + "Name").value,
    senderName: $(prefix + "SenderName").value,
    senderAddress: $(prefix + "SenderAddress").value,
    endpoint: $(prefix + "Endpoint").value,
    configJson: $(prefix + "Config").value,
    credentialJson: $(prefix + "Credential").value
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
  fields["普通配置"] = item.configJson;
  fields["敏感凭据"] = item.credentialConfigured ? "已配置" : "未配置";
  fields["状态"] = item.status;
  fields["创建时间"] = item.createdAt;
  fields["更新时间"] = item.updatedAt;
  return fields;
}

function renderNotificationEditFields(item) {
  return '<div class="form-grid notification-config-form">' +
    select("notifyEditProvider", "平台", notificationProviderOptions(item.channelType, false), item.providerCode) +
    input("notifyEditName", "配置名称", item.displayName) +
    input("notifyEditSenderName", item.channelType === "SMS" ? "短信签名" : "发件名称", item.senderName) +
    input("notifyEditSenderAddress", item.channelType === "SMS" ? "发送签名/扩展码" : "发件邮箱", item.senderAddress, { required: false }) +
    input("notifyEditEndpoint", "服务地址", item.endpoint, { required: false }) +
    (item.channelType === "SMS" ? renderSmsConfigFields("notifyEdit", item, item.providerCode, "edit") : "") +
    textarea("notifyEditConfig", item.channelType === "SMS" ? "附加配置 JSON" : "普通配置 JSON", item.configJson || "{}", { required: false }) +
    textarea("notifyEditCredential", item.channelType === "SMS" ? "附加凭据 JSON（留空不修改）" : "敏感凭据 JSON（留空不修改）", "", { required: false }) +
    '</div>';
}

function bindNotificationEditProvider(item) {
  if (item.channelType !== "SMS") return;
  var provider = $("notifyEditProvider");
  if (!provider) return;
  syncSmsProviderFields("notifyEdit", provider.value);
  provider.addEventListener("change", function () {
    syncSmsProviderFields("notifyEdit", provider.value);
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
  Array.prototype.forEach.call(document.querySelectorAll(".nav"), function (btn) {
    var active = btn.dataset.view === view;
    btn.classList.toggle("active", active);
    if (active) btn.setAttribute("aria-current", "page"); else btn.removeAttribute("aria-current");
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
  if (["payment", "sms", "email"].indexOf(state.channelsTab) < 0) {
    state.channelsTab = "payment";
  }
  var bodyId = state.channelsTab === "payment" ? "paymentConfigs" :
    state.channelsTab === "sms" ? "smsConfigs" : "emailConfigs";
  $("channels").innerHTML = renderTabs("channels", state.channelsTab, [
    { key: "payment", label: "支付配置", action: "switchChannelsTab" },
    { key: "sms", label: "短信配置", action: "switchChannelsTab" },
    { key: "email", label: "邮件配置", action: "switchChannelsTab" }
  ]) + '<div id="' + bodyId + '"></div>';
  if (state.channelsTab === "payment") return renderPaymentConfigs(currentPage("paymentConfigs"));
  if (state.channelsTab === "sms") return renderSmsConfigs(currentPage("smsConfigs"));
  return renderEmailConfigs(currentPage("emailConfigs"));
}

function renderCommerce() {
  if (["packages", "orders", "refunds"].indexOf(state.commerceTab) < 0) {
    state.commerceTab = "packages";
  }
  var bodyId = state.commerceTab;
  $("commerce").innerHTML = renderTabs("commerce", state.commerceTab, [
    { key: "packages", label: "套餐", action: "switchCommerceTab" },
    { key: "orders", label: "订单", action: "switchCommerceTab" },
    { key: "refunds", label: "退款", action: "switchCommerceTab" }
  ]) + '<div id="' + bodyId + '"></div>';
  if (state.commerceTab === "packages") return renderPackages();
  if (state.commerceTab === "orders") return renderOrders(currentPage("orders"));
  return renderRefunds(currentPage("refunds"));
}

function setBusy(busy) {
  document.body.classList.toggle("is-busy", !!busy);
  var refreshBtn = $("refreshBtn");
  if (refreshBtn) refreshBtn.disabled = !!busy;
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

function openRiskConfirm(title, message, actionLabel, onConfirm, options) {
  options = options || {};
  var reasonId = options.reasonId || "riskReason";
  var body = '<div class="risk-box">' + escapeHtml(message) + '</div>';
  if (options.reason !== false) {
    body += '<div class="form-grid">' + textarea(reasonId, "操作原因", "", { required: options.reasonRequired !== false, hint: "会写入后台操作日志，便于后续审计" }) + '</div>';
  }
  openModal(title, body,
    '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
    '<button class="danger" type="button" onclick="confirmRiskAction(\'' + reasonId + '\')">' + escapeHtml(actionLabel || "确认") + '</button>');
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
      '<div style="height:12px"></div>' +
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
    checkbox("needDeviceVip", "设备会员", false) +
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
      needDeviceVip: $("needDeviceVip").checked
    }
  }).then(function (data) {
    toast("创建成功，secret: " + data.appSecret);
    closeModal();
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
      "设备会员": item.needDeviceVip,
      "状态": item.status
    }) + sectionBlock("关键指标", statsGrid(data.stats)) +
      sectionBlock("套餐", compactTable([
        { title: "ID", key: "id" }, { title: "名称", key: "packageName" }, { title: "价格(分)", key: "priceCents" }, { title: "状态", render: function (r) { return badge(r.status); } }
      ], data.packages)) +
      sectionBlock("最近订单", compactTable([
        { title: "ID", key: "id" }, { title: "订单号", key: "orderNo" }, { title: "金额(分)", key: "amountCents" }, { title: "状态", render: function (r) { return badge(r.payStatus); } }
      ], data.recentOrders)) +
      sectionBlock("最近设备", compactTable([
        { title: "ID", key: "id" }, { title: "设备码", key: "deviceCode" }, { title: "用户", key: "userId" }, { title: "最近启动", key: "lastLaunchAt" }
      ], data.recentDevices));
    openModal("APP 详情", body,
      '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(item.appId || "") + '\')">复制 APP ID</button>' +
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

function resetSecret(id) {
  openRiskConfirm("确认重置 APP 密钥", "重置后旧密钥会失效，已接入项目需要同步更新配置。", "确认重置", function (reason) {
    api("/admin/apps/" + id + "/reset-secret", { method: "POST", body: { confirmReason: reason } }).then(function (data) {
      toast("新 secret: " + data.appSecret);
      closeModal();
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
  provider.addEventListener("change", function () {
    var d = notificationDefault(type, provider.value);
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
    $(prefix + "CreateConfig").value = d.configJson || "{}";
    $(prefix + "CreateCredential").value = d.credentialJson || "";
    if (type === "SMS") syncSmsProviderFields(prefix + "Create", provider.value);
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
        { title: "APP", key: "appId" },
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

function renderPaymentConfigForm(prefix, item, appOptions, mode) {
  return '<div class="form-grid payment-config-form">' +
    select(prefix + "AppId", "APP", appOptions, item.appId || "") +
    select(prefix + "Channel", "支付渠道", paymentChannelOptions(false), item.payChannel || "ALIPAY") +
    input(prefix + "Provider", "提供方编码", item.providerCode || providerDefaultFor(item.payChannel || "ALIPAY")) +
    input(prefix + "Merchant", "商户号", item.merchantId || "") +
    input(prefix + "ChannelApp", "渠道 APP ID", item.channelAppId || "", { required: false }) +
    input(prefix + "Notify", "回调地址", item.notifyUrl || "", { required: false }) +
    textarea(prefix + "Config", "普通配置 JSON", item.configJson || "{}", { required: false }) +
    textarea(prefix + "Credential", mode === "edit" ? "敏感凭据 JSON（留空不修改）" : "敏感凭据 JSON", mode === "edit" ? "" : (item.credentialJson || ""), { required: false }) +
    '</div>';
}

function openPaymentConfigCreate() {
  loadApps().then(function (apps) {
    if (!apps.length) throw new Error("请先创建 APP");
    var appOptions = apps.map(function (a) {
      return { value: a.appId, label: a.appId + " / " + a.appName };
    });
    var item = {
      appId: queryFilters("paymentConfigs").appId || apps[0].appId,
      payChannel: "ALIPAY",
      providerCode: providerDefaultFor("ALIPAY"),
      merchantId: "",
      channelAppId: "",
      notifyUrl: "",
      configJson: "{}",
      credentialJson: ""
    };
    openModal("新建支付配置", renderPaymentConfigForm("payCfgCreate", item, appOptions, "create"),
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="createPaymentConfig()">创建</button>');
    bindProviderDefault("payCfgCreateChannel", "payCfgCreateProvider");
  }).catch(function (err) { toast(err.message); });
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
    closeModal();
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
      "建议回调路径": "/api/payment/notify/" + item.payChannel,
      "普通配置": item.configJson,
      "敏感凭据": item.credentialConfigured ? "已配置" : "未配置",
      "状态": item.status,
      "创建时间": item.createdAt,
      "更新时间": item.updatedAt
    }), '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent('/api/payment/notify/' + item.payChannel) + '\')">复制回调路径</button>' +
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
    var appOptions = [{ value: item.appId, label: item.appId + " / 当前 APP" }];
    openModal("编辑支付配置", renderPaymentConfigForm("payCfgEdit", item, appOptions, "edit"),
      '<button class="secondary" type="button" onclick="closeModal()">取消</button>' +
      '<button type="button" onclick="savePaymentConfigEdit(' + id + ')">保存</button>');
    var app = $("payCfgEditAppId");
    if (app) app.disabled = true;
    var channel = $("payCfgEditChannel");
    if (channel) channel.disabled = true;
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
  return '<div class="form-grid notification-config-form">' +
    select(prefix + "Provider", "平台", notificationProviderOptions(type, false), item.providerCode) +
    input(prefix + "Name", "配置名称", item.displayName) +
    input(prefix + "SenderName", type === "SMS" ? "短信签名" : "发件名称", item.senderName) +
    input(prefix + "SenderAddress", type === "SMS" ? "发送签名/扩展码" : "发件邮箱", item.senderAddress, { required: false }) +
    input(prefix + "Endpoint", "服务地址", item.endpoint, { required: false }) +
    (type === "SMS" ? renderSmsConfigFields(prefix, item, item.providerCode, mode) : "") +
    textarea(prefix + "Config", type === "SMS" ? "附加配置 JSON" : "普通配置 JSON", item.configJson || "{}", { required: false }) +
    textarea(prefix + "Credential", mode === "edit" ? (type === "SMS" ? "附加凭据 JSON（留空不修改）" : "敏感凭据 JSON（留空不修改）") : (type === "SMS" ? "附加凭据 JSON" : "敏感凭据 JSON"), mode === "edit" ? "" : (item.credentialJson || ""), { required: false }) +
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
      code: $("smsSendCode").value
    }
  }).then(function (data) {
    toast(data.message || "短信已发送");
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
    openModal("通知配置详情", detailList(notificationDetailFields(item)), '<button class="secondary" type="button" onclick="closeModal()">关闭</button>');
  }).catch(function (err) { toast(err.message); });
}

function openNotificationConfigEdit(id) {
  api("/admin/notification-configs/" + id).then(function (item) {
    var footer = '<button class="secondary" type="button" onclick="closeModal()">取消</button>';
    if (item.channelType === "SMS") {
      footer += '<button class="secondary" type="button" onclick="openSmsSendCodeModal(' + id + ')">发送测试短信</button>';
    }
    footer += '<button type="button" onclick="saveNotificationConfigEdit(' + id + ', \'' + item.channelType + '\')">保存</button>';
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
        optionOf("MEMBERSHIP"),
        optionOf("FEATURE")
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
        { title: "ID", key: "id" }, { title: "订单号", key: "orderNo" }, { title: "金额(分)", key: "amountCents" }, { title: "状态", render: function (r) { return badge(r.payStatus); } }
      ], data.recentOrders)) +
      sectionBlock("最近启动", compactTable([
        { title: "ID", key: "id" }, { title: "平台", key: "platform" }, { title: "版本", key: "version" }, { title: "时长", render: function (r) { return formatDuration(r.durationSeconds); } }, { title: "时间", render: function (r) { return formatDateTime(r.createdAt); } }
      ], data.recentLaunches)) +
      sectionBlock("设备码修改历史", compactTable([
        { title: "时间", key: "createdAt" }, { title: "旧设备码", key: "oldDeviceCode" }, { title: "新设备码", key: "newDeviceCode" }, { title: "原因", key: "reason" }, { title: "管理员", key: "adminUsername" }
      ], data.recentDeviceCodeChanges));
    openModal("设备详情", body,
      '<button class="secondary" type="button" onclick="copyEncodedText(\'' + encodeURIComponent(item.deviceCode || "") + '\')">复制设备码</button>' +
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
          { title: "金额(分)", key: "amountCents" },
          { title: "退款(分)", key: "refundedAmountCents" },
          { title: "渠道", render: function (r) { return formatValue(r.payChannel); } },
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
  api("/admin/orders/" + id + "/timeline").then(function (data) {
    var item = data.order;
    var body = detailList({
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
      "关闭原因": item.closeReason,
      "会员状态": data.member ? data.member.status : "-"
    }) + sectionBlock("订单时间线", compactTable([
      { title: "时间", key: "happenedAt" },
      { title: "事件", key: "title" },
      { title: "状态", render: function (r) { return badge(r.status); } },
      { title: "金额(分)", key: "amountCents" },
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

function renderTools() {
  var launchCurl = "curl -X POST http://localhost:8888/api/device/launch -H 'Content-Type: application/json' -d '{\"appId\":\"<appId>\",\"deviceCode\":\"<deviceCode>\",\"platform\":\"ios\",\"version\":\"1.0.0\"}'";
  var payCurl = "curl -X POST http://localhost:8888/api/payment/create-order -H 'Content-Type: application/json' -d '{\"appId\":\"<appId>\",\"deviceId\":1,\"packageId\":1,\"payChannel\":\"ALIPAY\"}'";
  $("tools").innerHTML =
    panel("演示数据", '<p class="muted">创建演示设备码 APP、套餐、设备、订单，并标记支付成功。</p><button type="button" onclick="createDemo()">创建演示数据</button>') +
    '<div style="height:12px"></div>' +
    panel("设备码支付接入向导", '<p class="muted">外部项目接入时，按顺序调用：注册设备 → 上报启动 → 查询会员 → 创建支付宝订单。</p>' +
      '<div class="actions"><button type="button" onclick="copyEncodedText(\'' + encodeURIComponent(launchCurl) + '\')">复制启动上报 curl</button>' +
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
