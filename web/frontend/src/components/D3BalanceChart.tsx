import React, { useEffect, useRef, useState } from "react";
import * as d3 from "d3";
import { TokenLogo } from "./TokenLogo";

interface HistoryPoint {
  date: Date;
  dateStr: String;
  val: number;
}

interface D3BalanceChartProps {
  wallet: string;
}

export const D3BalanceChart: React.FC<D3BalanceChartProps> = ({ wallet }) => {
  const svgRef = useRef<SVGSVGElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [days, setDays] = useState<number>(30);
  const [hoverData, setHoverData] = useState<HistoryPoint | null>(null);

  // Generate 30 days of realistic history data
  const historyData = React.useMemo(() => {
    const data: HistoryPoint[] = [];
    const now = Date.now();
    const dayMs = 86_400_000;
    const baseVal = 2640;

    for (let i = 30; i >= 0; i--) {
      const d = new Date(now - i * dayMs);
      const dateStr = d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
      const trend = 1.0 + (30 - i) * 0.012;
      const noise = Math.sin(i * 0.7) * 95 + Math.cos(i * 0.4) * 60;
      const val = Math.max(1800, baseVal * trend + noise);
      data.push({ date: d, dateStr, val });
    }
    return data;
  }, [wallet]);

  const activeData = React.useMemo(() => {
    return historyData.slice(-days);
  }, [historyData, days]);

  const latestVal = activeData[activeData.length - 1]?.val || 2640;
  const startVal = activeData[0]?.val || latestVal;
  const netChange = latestVal - startVal;
  const netPercent = startVal > 0 ? (netChange / startVal) * 100 : 0;

  useEffect(() => {
    if (!svgRef.current || !containerRef.current || activeData.length === 0) return;

    const width = containerRef.current.clientWidth || 500;
    const height = 220;
    const margin = { top: 20, right: 20, bottom: 30, left: 45 };

    const svg = d3.select(svgRef.current);
    svg.selectAll("*").remove();

    svg.attr("width", width).attr("height", height);

    // Scales
    const xExtent = d3.extent(activeData, (d) => d.date) as [Date, Date];
    const yMin = (d3.min(activeData, (d) => d.val) || 2000) * 0.95;
    const yMax = (d3.max(activeData, (d) => d.val) || 3000) * 1.05;

    const xScale = d3.scaleTime().domain(xExtent).range([margin.left, width - margin.right]);
    const yScale = d3.scaleLinear().domain([yMin, yMax]).range([height - margin.bottom, margin.top]);

    // Gradient definition
    const defs = svg.append("defs");
    const gradient = defs
      .append("linearGradient")
      .attr("id", "d3-balance-grad")
      .attr("x1", "0%")
      .attr("y1", "0%")
      .attr("x2", "0%")
      .attr("y2", "100%");

    gradient.append("stop").attr("offset", "0%").attr("stop-color", "#00E6FF").attr("stop-opacity", 0.45);
    gradient.append("stop").attr("offset", "70%").attr("stop-color", "#0052FF").attr("stop-opacity", 0.12);
    gradient.append("stop").attr("offset", "100%").attr("stop-color", "#000000").attr("stop-opacity", 0);

    // D3 Area & Line Generators
    const area = d3
      .area<HistoryPoint>()
      .curve(d3.curveMonotoneX)
      .x((d) => xScale(d.date))
      .y0(height - margin.bottom)
      .y1((d) => yScale(d.val));

    const line = d3
      .line<HistoryPoint>()
      .curve(d3.curveMonotoneX)
      .x((d) => xScale(d.date))
      .y((d) => yScale(d.val));

    // Axes
    const xAxis = d3.axisBottom(xScale).ticks(5).tickFormat((d: any) => d3.timeFormat("%b %d")(d)).tickSize(0);
    const yAxis = d3.axisLeft(yScale).ticks(4).tickFormat((d: any) => `$${(d / 1000).toFixed(1)}k`).tickSize(0);

    // Grid lines
    svg
      .append("g")
      .attr("class", "grid")
      .attr("transform", `translate(0, ${height - margin.bottom})`)
      .call(xAxis)
      .selectAll("text")
      .style("fill", "var(--text-2)")
      .style("font-size", "11px");

    svg
      .append("g")
      .attr("class", "grid")
      .attr("transform", `translate(${margin.left}, 0)`)
      .call(yAxis)
      .selectAll("text")
      .style("fill", "var(--text-2)")
      .style("font-size", "11px");

    svg.selectAll(".domain").remove();

    // Draw Area
    svg
      .append("path")
      .datum(activeData)
      .attr("fill", "url(#d3-balance-grad)")
      .attr("d", area);

    // Draw Line
    svg
      .append("path")
      .datum(activeData)
      .attr("fill", "none")
      .attr("stroke", "#00E6FF")
      .attr("stroke-width", 3)
      .attr("filter", "drop-shadow(0px 0px 8px rgba(0, 230, 255, 0.6))")
      .attr("d", line);

    // Interactive Hover Overlay
    const overlay = svg
      .append("rect")
      .attr("width", width)
      .attr("height", height)
      .attr("fill", "none")
      .attr("pointer-events", "all");

    const guideLine = svg
      .append("line")
      .attr("stroke", "rgba(0, 230, 255, 0.5)")
      .attr("stroke-dasharray", "4")
      .style("opacity", 0);

    const focusCircle = svg
      .append("circle")
      .attr("r", 6)
      .attr("fill", "#00E6FF")
      .attr("stroke", "#FFFFFF")
      .attr("stroke-width", 2)
      .style("opacity", 0);

    overlay
      .on("mousemove touchmove", (event) => {
        const [mx] = d3.pointer(event);
        const dateAtX = xScale.invert(mx);
        const bisect = d3.bisector((d: HistoryPoint) => d.date).left;
        const index = bisect(activeData, dateAtX, 1);
        const d0 = activeData[index - 1];
        const d1 = activeData[index];
        let d = d0;
        if (d0 && d1) {
          d = dateAtX.getTime() - d0.date.getTime() > d1.date.getTime() - dateAtX.getTime() ? d1 : d0;
        }
        if (d) {
          const x = xScale(d.date);
          const y = yScale(d.val);
          guideLine.attr("x1", x).attr("x2", x).attr("y1", margin.top).attr("y2", height - margin.bottom).style("opacity", 1);
          focusCircle.attr("cx", x).attr("cy", y).style("opacity", 1);
          setHoverData(d);
        }
      })
      .on("mouseleave touchend", () => {
        guideLine.style("opacity", 0);
        focusCircle.style("opacity", 0);
        setHoverData(null);
      });
  }, [activeData]);

  const displayVal = hoverData ? hoverData.val : latestVal;

  return (
    <div className="card card-elev" style={{ marginTop: 14 }} ref={containerRef}>
      <div className="row between" style={{ marginBottom: 12 }}>
        <div className="row gap">
          <span style={{ fontSize: 20 }}>📈</span>
          <div>
            <div className="bold">Wallet Balance History</div>
            <div className="tiny muted">D3.js Interactive Monotone Line · Base Mainnet</div>
          </div>
        </div>
        <div className="row gap">
          {[7, 14, 30].map((d) => (
            <button
              key={d}
              className={`pill ${days === d ? "cyan" : ""}`}
              style={{ cursor: "pointer", border: "none", fontSize: 11, padding: "3px 8px" }}
              onClick={() => setDays(d)}
            >
              {d}D
            </button>
          ))}
        </div>
      </div>

      <div className="row between" style={{ alignItems: "flex-end", marginBottom: 12 }}>
        <div>
          <div className="tiny muted">{hoverData ? `Balance on ${hoverData.dateStr}` : `${days}-Day Portfolio Total`}</div>
          <div className="mono bold" style={{ fontSize: 24 }}>
            ${displayVal.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </div>
        </div>

        <span className={`pill ${netPercent >= 0 ? "green" : "red"}`}>
          {netPercent >= 0 ? "+" : ""}
          {netPercent.toFixed(1)}% (${netChange >= 0 ? "+" : ""}
          {netChange.toFixed(0)})
        </span>
      </div>

      <svg ref={svgRef} style={{ width: "100%", overflow: "visible" }} />

      <div style={{ height: 14 }} />

      <div className="tiny muted" style={{ marginBottom: 8 }}>
        Asset Composition:
      </div>
      <div className="row gap" style={{ flexWrap: "wrap" }}>
        <div className="row gap" style={{ background: "var(--card-bg)", padding: "6px 10px", borderRadius: 8, border: "1px solid var(--border)" }}>
          <TokenLogo symbol="AGL" size={20} />
          <div className="col">
            <span className="tiny bold">AGL</span>
            <span className="tiny muted">$1,062.88</span>
          </div>
        </div>
        <div className="row gap" style={{ background: "var(--card-bg)", padding: "6px 10px", borderRadius: 8, border: "1px solid var(--border)" }}>
          <TokenLogo symbol="wAGL" size={20} />
          <div className="col">
            <span className="tiny bold">wAGL</span>
            <span className="tiny muted">$272.00</span>
          </div>
        </div>
        <div className="row gap" style={{ background: "var(--card-bg)", padding: "6px 10px", borderRadius: 8, border: "1px solid var(--border)" }}>
          <TokenLogo symbol="ETH" size={20} />
          <div className="col">
            <span className="tiny bold">ETH</span>
            <span className="tiny muted">$1,449.00</span>
          </div>
        </div>
      </div>
    </div>
  );
};
