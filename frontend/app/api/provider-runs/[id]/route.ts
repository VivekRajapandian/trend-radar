import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.TREND_RADAR_API_BASE_URL ?? "http://localhost:8080";

type RouteContext = {
  params: Promise<{ id: string }>;
};

export async function GET(_request: NextRequest, context: RouteContext) {
  const { id } = await context.params;
  const backendUrl = new URL(`/api/provider-runs/${id}`, API_BASE_URL);

  try {
    const response = await fetch(backendUrl, {
      headers: { Accept: "application/json" },
      cache: "no-store"
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
