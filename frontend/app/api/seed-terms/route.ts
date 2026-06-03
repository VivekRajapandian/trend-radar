import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.TREND_RADAR_API_BASE_URL ?? "http://localhost:8080";

export async function GET(request: NextRequest) {
  const backendUrl = new URL("/api/seed-terms", API_BASE_URL);
  request.nextUrl.searchParams.forEach((value, key) => {
    backendUrl.searchParams.set(key, value);
  });

  return proxyJson(backendUrl);
}

export async function POST(request: NextRequest) {
  const backendUrl = new URL("/api/seed-terms", API_BASE_URL);

  return proxyJson(backendUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: await request.text()
  });
}

async function proxyJson(backendUrl: URL, init?: RequestInit) {
  try {
    const response = await fetch(backendUrl, {
      headers: { Accept: "application/json", ...init?.headers },
      cache: "no-store",
      ...init
    });
    const body = await response.text();

    return new NextResponse(body, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("Content-Type") ?? "application/json" }
    });
  } catch {
    return NextResponse.json({ message: "Unable to reach TrendRadar backend" }, { status: 502 });
  }
}
