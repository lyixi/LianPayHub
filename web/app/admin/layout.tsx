import type { Metadata } from "next";

export const metadata: Metadata = {
  title: { absolute: "懒一夕官网运营后台" },
  description: "懒一夕官网内容与产品运营后台",
  robots: { index: false, follow: false },
};

export default function AdminLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return children;
}
