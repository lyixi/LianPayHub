"use client";

import { Image } from "@tiptap/extension-image";
import { Placeholder } from "@tiptap/extension-placeholder";
import { Table, TableCell, TableHeader, TableRow } from "@tiptap/extension-table";
import { EditorContent, useEditor, type Editor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import {
  Bold,
  Code2,
  Columns3,
  Heading2,
  Heading3,
  ImagePlus,
  Italic,
  Link2,
  List,
  ListOrdered,
  Minus,
  Quote,
  Redo2,
  Rows3,
  Strikethrough,
  Table2,
  Trash2,
  Undo2,
  X,
} from "lucide-react";
import type { ReactNode } from "react";
import { useState } from "react";

type EditorMedia = { id: number; url: string; title?: string; altText?: string; fileName?: string };

type ToolbarButtonProps = {
  children: ReactNode;
  label: string;
  active?: boolean;
  disabled?: boolean;
  onClick: () => void;
};

function ToolbarButton({ children, label, active = false, disabled = false, onClick }: ToolbarButtonProps) {
  return <button type="button" title={label} aria-label={label} aria-pressed={active} disabled={disabled} className={active ? "is-active" : ""} onClick={onClick}>{children}</button>;
}

function TableTools({ editor }: { editor: Editor }) {
  if (!editor.isActive("table")) return null;
  return <div className="rich-toolbar-group rich-table-tools" aria-label="表格操作">
    <ToolbarButton label="增加一行" onClick={() => editor.chain().focus().addRowAfter().run()}><Rows3 size={17}/></ToolbarButton>
    <ToolbarButton label="增加一列" onClick={() => editor.chain().focus().addColumnAfter().run()}><Columns3 size={17}/></ToolbarButton>
    <ToolbarButton label="删除当前行" onClick={() => editor.chain().focus().deleteRow().run()}><Rows3 size={17}/><Minus size={10}/></ToolbarButton>
    <ToolbarButton label="删除当前列" onClick={() => editor.chain().focus().deleteColumn().run()}><Columns3 size={17}/><Minus size={10}/></ToolbarButton>
    <ToolbarButton label="删除表格" onClick={() => editor.chain().focus().deleteTable().run()}><Trash2 size={17}/></ToolbarButton>
  </div>;
}

export function RichTextEditor({ name, initialContent = "", media = [] }: { name: string; initialContent?: string; media?: EditorMedia[] }) {
  const [html, setHtml] = useState(initialContent);
  const [showImages, setShowImages] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const [imageAlt, setImageAlt] = useState("");
  const editor = useEditor({
    immediatelyRender: false,
    content: initialContent || "<p></p>",
    extensions: [
      StarterKit.configure({
        link: {
          openOnClick: false,
          HTMLAttributes: { target: "_blank", rel: "noopener noreferrer" },
        },
      }),
      Image.configure({ allowBase64: false, HTMLAttributes: { loading: "lazy" } }),
      Table.configure({ resizable: true, HTMLAttributes: { class: "content-table" } }),
      TableRow,
      TableHeader,
      TableCell,
      Placeholder.configure({ placeholder: "介绍产品、使用场景和核心能力。可以插入图片、引用、列表与表格。" }),
    ],
    editorProps: { attributes: { class: "rich-editor-content", spellcheck: "true" } },
    onUpdate: ({ editor: nextEditor }) => setHtml(nextEditor.getHTML()),
  });

  if (!editor) return <div className="rich-editor-loading">正在准备编辑器…</div>;

  const editLink = () => {
    const previous = editor.getAttributes("link").href as string | undefined;
    const url = window.prompt("输入链接地址", previous || "https://");
    if (url === null) return;
    if (!url.trim()) {
      editor.chain().focus().extendMarkRange("link").unsetLink().run();
      return;
    }
    editor.chain().focus().extendMarkRange("link").setLink({ href: url.trim() }).run();
  };
  const addImage = (url: string, alt = "") => {
    if (!url.trim()) return;
    editor.chain().focus().setImage({ src: url.trim(), alt: alt.trim() }).run();
    setImageUrl("");
    setImageAlt("");
    setShowImages(false);
  };

  return <div className="rich-editor">
    <input type="hidden" name={name} value={html}/>
    <div className="rich-toolbar" role="toolbar" aria-label="富文本格式">
      <div className="rich-toolbar-group">
        <ToolbarButton label="撤销" disabled={!editor.can().chain().focus().undo().run()} onClick={() => editor.chain().focus().undo().run()}><Undo2 size={17}/></ToolbarButton>
        <ToolbarButton label="重做" disabled={!editor.can().chain().focus().redo().run()} onClick={() => editor.chain().focus().redo().run()}><Redo2 size={17}/></ToolbarButton>
      </div>
      <div className="rich-toolbar-group">
        <ToolbarButton label="二级标题" active={editor.isActive("heading", { level: 2 })} onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}><Heading2 size={17}/></ToolbarButton>
        <ToolbarButton label="三级标题" active={editor.isActive("heading", { level: 3 })} onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}><Heading3 size={17}/></ToolbarButton>
        <ToolbarButton label="粗体" active={editor.isActive("bold")} onClick={() => editor.chain().focus().toggleBold().run()}><Bold size={17}/></ToolbarButton>
        <ToolbarButton label="斜体" active={editor.isActive("italic")} onClick={() => editor.chain().focus().toggleItalic().run()}><Italic size={17}/></ToolbarButton>
        <ToolbarButton label="删除线" active={editor.isActive("strike")} onClick={() => editor.chain().focus().toggleStrike().run()}><Strikethrough size={17}/></ToolbarButton>
      </div>
      <div className="rich-toolbar-group">
        <ToolbarButton label="无序列表" active={editor.isActive("bulletList")} onClick={() => editor.chain().focus().toggleBulletList().run()}><List size={17}/></ToolbarButton>
        <ToolbarButton label="有序列表" active={editor.isActive("orderedList")} onClick={() => editor.chain().focus().toggleOrderedList().run()}><ListOrdered size={17}/></ToolbarButton>
        <ToolbarButton label="引用" active={editor.isActive("blockquote")} onClick={() => editor.chain().focus().toggleBlockquote().run()}><Quote size={17}/></ToolbarButton>
        <ToolbarButton label="代码块" active={editor.isActive("codeBlock")} onClick={() => editor.chain().focus().toggleCodeBlock().run()}><Code2 size={17}/></ToolbarButton>
        <ToolbarButton label="分隔线" onClick={() => editor.chain().focus().setHorizontalRule().run()}><Minus size={17}/></ToolbarButton>
      </div>
      <div className="rich-toolbar-group">
        <ToolbarButton label="链接" active={editor.isActive("link")} onClick={editLink}><Link2 size={17}/></ToolbarButton>
        <ToolbarButton label="插入图片" active={showImages} onClick={() => setShowImages(value => !value)}><ImagePlus size={17}/></ToolbarButton>
        <ToolbarButton label="插入表格" onClick={() => editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()}><Table2 size={17}/></ToolbarButton>
      </div>
      <TableTools editor={editor}/>
    </div>
    {showImages && <div className="rich-image-picker">
      <header><div><b>插入图片</b><span>从媒体库选择，或粘贴图片地址。</span></div><button type="button" onClick={() => setShowImages(false)} aria-label="关闭图片选择"><X size={16}/></button></header>
      {media.length > 0 && <div className="rich-image-grid">{media.map(asset => <button type="button" key={asset.id} onClick={() => addImage(asset.url, asset.altText || asset.title || asset.fileName || "产品图片")} title={asset.title || asset.fileName || "插入图片"}><img src={asset.url} alt=""/><span>{asset.title || asset.fileName || "未命名图片"}</span></button>)}</div>}
      <div className="rich-image-url"><label>图片地址<input value={imageUrl} onChange={event => setImageUrl(event.target.value)} placeholder="/uploads/product.webp"/></label><label>图片说明<input value={imageAlt} onChange={event => setImageAlt(event.target.value)} placeholder="描述图片内容"/></label><button type="button" className="admin-primary" disabled={!imageUrl.trim()} onClick={() => addImage(imageUrl, imageAlt)}>插入</button></div>
    </div>}
    <EditorContent editor={editor}/>
  </div>;
}
