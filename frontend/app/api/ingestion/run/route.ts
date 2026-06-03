import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL = process.env.TREND_RADAR_API_BASE_URL ?? "http://localhost:8080";

export async function POST(request: NextRequest) {
  const backendUrl = new URL("/api/ingestion/run", API_BASE_URL);
  request.nextUrl.searchParams.forEach((value, key) => {
    backendUrl.searchParams.set(key, value);
  });

  try {
    const response = await fetch(backendUrl, {
      method: "POST",
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
