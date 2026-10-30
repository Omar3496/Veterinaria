import React from 'react';

export default function Skeleton({ className = "", variant = "rect" }) {
  const baseClasses = "animate-pulse bg-slate-200/60 border border-slate-100";
  
  const variants = {
    rect: "rounded-xl",
    circle: "rounded-full",
    text: "rounded-md h-4"
  };

  return (
    <div className={`${baseClasses} ${variants[variant]} ${className}`} />
  );
}

// Skeleton structure for Tables
export function TableSkeleton({ rows = 5, columns = 4 }) {
  return (
    <div className="w-full bg-white rounded-2xl border border-slate-100 p-4 overflow-hidden shadow-sm">
      <div className="flex border-b border-slate-100 pb-4 mb-4">
        {Array(columns).fill(0).map((_, i) => (
          <Skeleton key={`h-${i}`} variant="text" className="h-5 w-24 mx-4 flex-1" />
        ))}
      </div>
      <div className="space-y-4">
        {Array(rows).fill(0).map((_, i) => (
          <div key={`r-${i}`} className="flex items-center space-x-4">
            {Array(columns).fill(0).map((_, j) => (
              <Skeleton key={`c-${i}-${j}`} variant="rect" className="h-8 flex-1 bg-slate-100/70 border-none" />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
