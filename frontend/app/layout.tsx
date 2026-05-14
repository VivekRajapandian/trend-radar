import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "TrendRadar",
  description: "Product opportunity intelligence for online sellers"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
