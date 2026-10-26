/* ACE Data operations and schemas are bundled from the public documentation. */
var aceOperations = [];
var aceDrafts = {};
var aceActiveOperation = '';
var aceRequestPending = false;

function openAceDataWorkbench(platformId) {
  api('/admin/ai-platforms/acedata/operations').then(function (operations) {
    aceOperations = operations;
    aceDrafts = {};
    aceActiveOperation = '';
    var categories = ['文字与多模态', '图片', '视频', '音频与音乐', '任务查询'];
    openModal('ACE Data · 接口工作台',
      '<div class="ace-workbench" data-platform="' + platformId + '">' +
      '<div class="form-grid">' + select('aceCategory', '能力类型', categories.map(function (c) { return { value: c, label: c }; }), categories[0]) +
      select('aceOperation', '接口', [], '') + '</div>' +
      '<p id="aceDescription" class="muted"></p><p><a id="aceDocs" target="_blank" rel="noopener noreferrer">查看接口文档与参数说明 ↗</a></p>' +
      '<label class="field">请求参数（JSON）<textarea id="aceParameters" class="ace-json" rows="14" spellcheck="false"></textarea></label>' +
      '<p class="muted">生成会消耗 ACE Data 额度。视频等长任务建议设置 async: true，保存 task_id 后到任务查询接口获取结果。当前支持非流式 JSON 请求。</p>' +
      '<details><summary>参数说明</summary><div id="aceSchema"></div></details>' +
      '<div id="aceResult" role="status" aria-live="polite"></div></div>',
      '<button class="secondary" type="button" onclick="closeModal()">关闭</button>' +
      '<button id="aceRun" type="button" onclick="runAceData(' + platformId + ')">发送请求</button>');
    $('aceCategory').addEventListener('change', aceFilterOperations);
    $('aceOperation').addEventListener('change', aceSelectOperation);
    aceFilterOperations();
  }).catch(function (error) { toast(error.message); });
}

function aceSaveDraft() {
  if (aceActiveOperation && $('aceParameters')) aceDrafts[aceActiveOperation] = $('aceParameters').value;
}

function aceFilterOperations() {
  aceSaveDraft();
  var category = $('aceCategory').value;
  $('aceOperation').innerHTML = aceOperations.filter(function (o) { return o.category === category; }).map(function (o) {
    return '<option value="' + escapeAttr(o.id) + '">' + escapeHtml(o.title) + '</option>';
  }).join('');
  aceSelectOperation();
}

function aceResolveSchema(schema, operation) {
  if (schema && schema.$ref && schema.$ref.indexOf('#/components/schemas/') === 0) {
    return (operation.components.schemas || {})[schema.$ref.split('/').pop()] || schema;
  }
  return schema || {};
}

function aceExample(schema, operation, depth) {
  schema = aceResolveSchema(schema, operation);
  if (depth > 6) return null;
  if (schema.example !== undefined) return schema.example;
  if (schema.default !== undefined) return schema.default;
  if (schema.enum && schema.enum.length) return schema.enum[0];
  if (schema.oneOf || schema.anyOf) return aceExample((schema.oneOf || schema.anyOf)[0], operation, depth + 1);
  if (schema.type === 'object' || schema.properties) {
    var result = {};
    (schema.required || []).forEach(function (key) { result[key] = aceExample((schema.properties || {})[key], operation, depth + 1); });
    // Include documented examples for optional fields to make the template useful.
    Object.keys(schema.properties || {}).forEach(function (key) {
      var property = schema.properties[key];
      if (result[key] === undefined && (property.example !== undefined || key === 'model')) result[key] = aceExample(property, operation, depth + 1);
    });
    return result;
  }
  if (schema.type === 'array') return [aceExample(schema.items, operation, depth + 1)];
  if (schema.type === 'boolean') return false;
  if (schema.type === 'number' || schema.type === 'integer') return schema.minimum || 1;
  return '';
}

function aceSelectOperation() {
  aceSaveDraft();
  var operation = aceOperations.find(function (o) { return o.id === $('aceOperation').value; });
  if (!operation) return;
  aceActiveOperation = operation.id;
  $('aceDescription').textContent = operation.description;
  $('aceDocs').href = operation.docs;
  var example = aceExample(operation.schema, operation, 0) || {};
  if (example.stream !== undefined) example.stream = false;
  if (operation.category === '视频' && operation.schema.properties && operation.schema.properties.async) example.async = true;
  $('aceParameters').value = aceDrafts[operation.id] || JSON.stringify(example, null, 2);
  var schema = aceResolveSchema(operation.schema, operation);
  $('aceSchema').innerHTML = table([
    { title: '参数', render: function (r) { return escapeHtml(r.name) + ((schema.required || []).indexOf(r.name) >= 0 ? ' *' : ''); } },
    { title: '类型', render: function (r) { return escapeHtml(r.type || (r.$ref ? 'object' : '')); } },
    { title: '说明', render: function (r) { return escapeHtml(r.description || '') + (r.enum ? ' · ' + escapeHtml(r.enum.join(' / ')) : ''); } }
  ], Object.keys(schema.properties || {}).map(function (key) { return Object.assign({ name: key }, schema.properties[key]); }));
}

function runAceData(platformId) {
  if (aceRequestPending) return;
  var parameters;
  try {
    parameters = JSON.parse($('aceParameters').value);
    if (!parameters || Array.isArray(parameters) || typeof parameters !== 'object') throw new Error();
  } catch (error) { toast('请求参数必须是有效的 JSON 对象'); return; }
  var operation = $('aceOperation').value;
  var resultElement = $('aceResult');
  var runButton = $('aceRun');
  aceRequestPending = true;
  runButton.disabled = true;
  resultElement.textContent = '请求处理中，请勿重复提交…';
  api('/admin/ai-platforms/' + platformId + '/acedata/invoke', { method: 'POST', body: { operation: operation, parameters: parameters } })
    .then(function (result) {
      // Keep results attached to their original workbench when a modal is closed.
      resultElement.innerHTML = '<h3>' + escapeHtml(operation) + ' · 返回结果</h3><pre class="ace-json ace-result">' + escapeHtml(JSON.stringify(result, null, 2)) + '</pre>';
      if (result && result.base64 && /^(audio|image|video)\/[a-z0-9.+-]+$/i.test(result.contentType || '')) {
        var link = document.createElement('a');
        link.href = 'data:' + result.contentType + ';base64,' + result.base64;
        link.download = 'acedata-result.' + result.contentType.split('/')[1];
        link.textContent = '下载生成文件';
        resultElement.prepend(link);
      }
      // Only expose safe, absolute HTTP(S) result links.
      var urls = [];
      function collect(value, depth) {
        if (depth > 12 || urls.length >= 20) return;
        if (typeof value === 'string' && /^https?:\/\/\S+$/i.test(value)) {
          try { var url = new URL(value); if (url.protocol === 'https:' || url.protocol === 'http:') urls.push(url.href); } catch (e) { /* Ignore invalid URLs. */ }
        } else if (value && typeof value === 'object') Object.keys(value).forEach(function (k) { collect(value[k], depth + 1); });
      }
      collect(result, 0);
      urls.filter(function (url, index) { return urls.indexOf(url) === index; }).forEach(function (url) {
        var link = document.createElement('a'); link.href = url; link.target = '_blank'; link.rel = 'noopener noreferrer';
        link.textContent = '打开结果文件 ↗'; link.className = 'ace-result-link'; resultElement.appendChild(link);
      });
    }).catch(function (error) { resultElement.textContent = error.message; })
    .finally(function () { aceRequestPending = false; runButton.disabled = false; });
}
