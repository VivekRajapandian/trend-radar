import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.TREND_RADAR_API_BASE_URL ?? "http://localhost:8080";

type RouteContext = {
  params: Promise<{ id: string }>;
};

export async function PATCH(request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  const backendUrl = new URL(`/api/seed-terms/${id}`, API_BASE_URL);

  return proxyJson(backendUrl, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: await request.text()
  });
}

export async function DELETE(_request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  const backendUrl = new URL(`/api/seed-terms/${id}`, API_BASE_URL);

  return proxyJson(backendUrl, { method: "DELETE" });
}

async function proxyJson(backendUrl: URL, init: RequestInit) {
  try {
    const response = await fetch(backendUrl, {
      headers: { Accept: "application/json", ...init.headers },
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
