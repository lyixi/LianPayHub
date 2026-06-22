var qs = new URLSearchParams(location.search);
var pageData = null;
var selectedProduct = null;
var selectedPlan = null;
var selectedChannel = 'ALIPAY';
var fixedUserId = qs.get('userId') || '';
var fixedDeviceCode = qs.get('deviceCode') || '';
var fixedProductType = qs.get('productType') || '';
var fixedProductCode = qs.get('productCode') || '';
var fixedPlanCode = qs.get('planCode') || '';
var fixedPageSlug = qs.get('pageSlug') || '';
var currentSlug = fixedPageSlug || location.pathname.split('/').filter(Boolean).pop();
var filteredProducts = [];
var filteredPlans = [];

if (qs.get('preview') === '1') {
  var previewPlans = JSON.parse(qs.get('plans') || '[]');
  var previewProduct = {
    id: 1,
    productName: qs.get('name') || '预览商品',
    description: qs.get('desc') || '',
    benefitsText: qs.get('benefits') || ''
  };
  document.getElementById('title').textContent = previewProduct.productName;
  document.getElementById('subtitle').textContent = previewProduct.description || '选择方案后即可查看支付区效果';
  renderBenefits(previewProduct.benefitsText);
  renderContext({ appName: '预览模式' });
  renderProducts([previewProduct], previewPlans);
  renderChannels(['ALIPAY', 'WECHAT']);
} else {
  fetch('/public/purchase-pages/' + encodeURIComponent(currentSlug))
    .then(function (r) { return r.json(); })
    .then(function (res) {
      if (res.code !== 0) throw new Error(res.message || '加载失败');
      pageData = res.data || {};
      document.getElementById('title').textContent = pageData.page.title || '购买页';
      document.getElementById('subtitle').textContent = pageData.page.subtitle || '';
      renderContext(pageData.app || {});
      filteredProducts = (pageData.products || []).filter(function (p) {
        if (fixedProductCode && p.productCode !== fixedProductCode) return false;
        if (fixedProductType && p.productType !== fixedProductType) return false;
        return true;
      });
      var productIds = filteredProducts.map(function (p) { return Number(p.id); });
      filteredPlans = (pageData.plans || []).filter(function (plan) {
        if (productIds.indexOf(Number(plan.productId || 1)) < 0) return false;
        if (fixedPlanCode && plan.planCode !== fixedPlanCode) return false;
        return true;
      });
      renderProducts(filteredProducts, filteredPlans);
      renderChannels(pageData.payChannels || ['ALIPAY']);
    })
    .catch(function (err) {
      document.getElementById('products').innerHTML = '<div class="empty">' + escapeHtml(err.message) + '</div>';
    });
}

document.getElementById('payBtn').addEventListener('click', submitPurchase);

function renderBenefits(text) {
  var box = document.getElementById('benefits');
  var items = String(text || '').split(/\n+/).map(function (x) { return x.trim(); }).filter(Boolean);
  if (!items.length) {
    box.classList.add('hidden');
    box.innerHTML = '';
    return;
  }
  box.classList.remove('hidden');
  box.innerHTML = items.map(function (item) { return '<div>' + escapeHtml(item) + '</div>'; }).join('');
}

function renderContext(app) {
  var bindText = fixedUserId ? '当前将为你的账号开通' : (fixedDeviceCode ? '当前将为你的设备开通' : '等待程序传入账号或设备信息');
  document.getElementById('contextBox').innerHTML =
    '<div><strong>' + escapeHtml(app.appName || '') + '</strong></div>' +
    '<div class="hint" style="margin-top:8px">' + escapeHtml(bindText) + '</div>' +
    '<div class="hint" style="margin-top:8px">身份信息由程序自动携带，无需手动输入。</div>';
}

function renderProducts(products, plans) {
  var html = products.map(function (product) {
    var rows = plans.filter(function (plan) { return Number(plan.productId || 1) === Number(product.id || 1); });
    return '<section class="product">' +
      '<h2>' + escapeHtml(product.productName) + '</h2>' +
      '<p class="muted">' + escapeHtml(product.description || '') + '</p>' +
      (product.benefitsText ? '<div class="meta">' + escapeHtml(product.benefitsText).replace(/\n/g, '<br>') + '</div>' : '') +
      '<div class="plans">' + rows.map(function (plan) {
        var planBenefits = plan.benefitsText || product.benefitsText || '';
        return '<article class="plan" data-plan-id="' + plan.id + '" onclick="selectPlan(\'' + encodeURIComponent(JSON.stringify(product)).replace(/'/g, '%27') + '\',\'' + encodeURIComponent(JSON.stringify(plan)).replace(/'/g, '%27') + '\')">' +
          (plan.badgeText ? '<span class="badge">' + escapeHtml(plan.badgeText) + '</span>' : '') +
          '<div class="price">¥' + (Number(plan.priceCents || 0) / 100).toFixed(2) + '</div>' +
          (plan.originalPriceCents ? '<div class="origin-price">¥' + (Number(plan.originalPriceCents) / 100).toFixed(2) + '</div>' : '') +
          '<div><strong>' + escapeHtml(plan.planName || '') + '</strong></div>' +
          '<div class="meta">' +
            (plan.durationDays ? ('时长 ' + plan.durationDays + ' 天<br>') : '') +
            (plan.creditAmount ? ('算力 ' + plan.creditAmount + '<br>') : '') +
            (planBenefits ? escapeHtml(planBenefits).replace(/\n/g, '<br>') : '') +
          '</div>' +
        '</article>';
      }).join('') + '</div>' +
    '</section>';
  }).join('');
  document.getElementById('products').innerHTML = html || '<div class="empty">暂无可购买商品</div>';
  if (!selectedPlan) {
    var firstProduct = products && products.length ? products[0] : null;
    var firstPlan = plans && plans.length ? plans[0] : null;
    if (firstProduct && firstPlan) {
      selectPlan(encodeURIComponent(JSON.stringify(firstProduct)).replace(/'/g, '%27'), encodeURIComponent(JSON.stringify(firstPlan)).replace(/'/g, '%27'));
    }
  }
}

function renderChannels(channels) {
  var box = document.getElementById('channelBox');
  var list = (channels || []).length ? channels : ['ALIPAY'];
  box.innerHTML = '<div class="hint" style="margin-bottom:10px">选择支付方式</div>' + list.map(function (channel, index) {
    var checked = index === 0 ? ' checked' : '';
    if (index === 0) selectedChannel = channel;
    return '<label class="channel-option' + (index === 0 ? ' active' : '') + '">' +
      '<input type="radio" name="payChannel" value="' + escapeHtml(channel) + '"' + checked + ' onchange="selectChannel(\'' + escapeHtml(channel) + '\',this)">' +
      '<span>' + channelLabel(channel) + '</span>' +
    '</label>';
  }).join('');
}

function channelLabel(channel) {
  return channel === 'ALIPAY' ? '支付宝支付' : (channel === 'WECHAT' ? '微信支付' : channel);
}

function selectChannel(channel, input) {
  selectedChannel = channel;
  Array.prototype.forEach.call(document.querySelectorAll('.channel-option'), function (item) { item.classList.remove('active'); });
  if (input && input.parentNode) input.parentNode.classList.add('active');
}

function selectPlan(productEncoded, planEncoded) {
  selectedProduct = JSON.parse(decodeURIComponent(String(productEncoded || '{}')));
  selectedPlan = JSON.parse(decodeURIComponent(String(planEncoded || '{}')));
  Array.prototype.forEach.call(document.querySelectorAll('.plan'), function (item) { item.classList.remove('active'); });
  var active = document.querySelector('.plan[data-plan-id="' + selectedPlan.id + '"]');
  if (active) active.classList.add('active');
  renderBenefits(selectedPlan.benefitsText || selectedProduct.benefitsText || '');
  renderSelection();
}

function renderSelection() {
  var box = document.getElementById('selectionBox');
  if (!selectedPlan) {
    box.className = 'selection-box empty';
    box.innerHTML = '请先选择一个方案';
    document.getElementById('payBtn').disabled = true;
    return;
  }
  box.className = 'selection-box';
  box.innerHTML = '<div class="hint">当前已选</div>' +
    '<div class="price">¥' + (Number(selectedPlan.priceCents || 0) / 100).toFixed(2) + '</div>' +
    (selectedPlan.originalPriceCents ? '<div class="origin-price">¥' + (Number(selectedPlan.originalPriceCents) / 100).toFixed(2) + '</div>' : '') +
    '<div><strong>' + escapeHtml(selectedProduct.productName || '') + ' / ' + escapeHtml(selectedPlan.planName || '') + '</strong></div>' +
    '<div class="meta">' +
      (selectedPlan.durationDays ? ('时长 ' + selectedPlan.durationDays + ' 天<br>') : '') +
      (selectedPlan.creditAmount ? ('算力 ' + selectedPlan.creditAmount + '<br>') : '') +
      ((selectedPlan.benefitsText || selectedProduct.benefitsText) ? escapeHtml(selectedPlan.benefitsText || selectedProduct.benefitsText).replace(/\n/g, '<br>') : '') +
    '</div>';
  document.getElementById('payBtn').disabled = false;
}

function submitPurchase() {
  if (!selectedPlan || !selectedProduct) return;
  var result = document.getElementById('payResult');
  result.classList.remove('hidden');
  if (qs.get('preview') === '1') {
    result.innerHTML = '<div>预览模式下不真实下单。</div><div style="margin-top:8px">这里会展示订单号、二维码链接和打开支付页按钮。</div>';
    return;
  }
  if (!fixedUserId && !fixedDeviceCode) {
    result.innerHTML = '<div>缺少程序透传的账号或设备参数，无法下单。</div>';
    return;
  }
  fetch('/public/purchase-orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pageSlug: pageData.page.pageSlug,
      productId: selectedProduct.id,
      productCode: selectedProduct.productCode,
      productType: selectedProduct.productType,
      planId: selectedPlan.id,
      planCode: selectedPlan.planCode,
      userId: fixedUserId ? Number(fixedUserId) : null,
      deviceCode: fixedDeviceCode || null,
      payChannel: selectedChannel
    })
  }).then(function (r) { return r.json(); }).then(function (res) {
    if (res.code !== 0) throw new Error(res.message || '下单失败');
    var data = res.data || {};
    var params = data.paymentParams || {};
    var payUrl = params.qrCode || params.payUrl || params.browserUrl || '';
    result.innerHTML = '<div>订单号：' + escapeHtml(data.orderNo || '') + '</div>' +
      '<div style="margin-top:8px">支付链接：' + (payUrl ? '<a href="' + escapeHtml(payUrl) + '" target="_blank" rel="noopener">' + escapeHtml(payUrl) + '</a>' : '暂无') + '</div>' +
      '<div class="actions" style="margin-top:12px">' +
      (payUrl ? '<a class="secondary" href="' + escapeHtml('/pay.html?orderNo=' + encodeURIComponent(data.orderNo) + '&payUrl=' + encodeURIComponent(payUrl)) + '" target="_blank">打开支付页</a>' : '') +
      '</div>';
  }).catch(function (err) {
    result.innerHTML = '<div>' + escapeHtml(err.message) + '</div>';
  });
}

function escapeHtml(value) {
  return String(value == null ? '' : value).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[c];
  });
}
