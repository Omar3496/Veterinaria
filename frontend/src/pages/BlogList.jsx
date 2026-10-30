import React from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Clock, ArrowRight } from 'lucide-react';
import { blogData } from '../data/blogData';
import SiteHeader from '../components/SiteHeader';

const PawIcon = ({ className }) => (
  <svg viewBox="0 -32 512.00011 512" className={className} fill="currentColor">
    <path d="m342.382812 239.351562c-23.039062-35.941406-62.277343-57.402343-104.964843-57.402343s-81.925781 21.460937-104.960938 57.402343l-55.515625 86.605469c-9.210937 14.367188-13.460937 30.96875-12.292968 47.996094 1.167968 17.03125 7.648437 32.890625 18.738281 45.875 11.097656 12.972656 25.761719 21.839844 42.40625 25.644531 16.644531 3.804688 33.707031 2.179688 49.339843-4.691406l1.046876-.464844c39.335937-16.945312 84.285156-16.789062 123.519531.464844 10.121093 4.449219 20.84375 6.699219 31.664062 6.699219 5.882813 0 11.800781-.667969 17.664063-2.003907 16.644531-3.800781 31.308594-12.667968 42.410156-25.644531 11.09375-12.972656 17.578125-28.835937 18.75-45.871093 1.171875-17.035157-3.078125-33.632813-12.289062-48.007813zm0 0" />
    <path d="m91.894531 239.238281c16.515625-6.34375 29.0625-19.652343 35.328125-37.476562 5.964844-16.960938 5.476563-36.109375-1.378906-53.921875-6.859375-17.800782-19.335938-32.332032-35.132812-40.921875-16.59375-9.019531-34.824219-10.488281-51.3125-4.132813-33.171876 12.753906-48.394532 53.746094-33.929688 91.398438 11.554688 29.96875 38.503906 48.886718 65.75 48.886718 6.957031 0 13.933594-1.234374 20.675781-3.832031zm0 0" />
    <path d="m199.613281 171.386719c41.46875 0 75.207031-38.4375 75.207031-85.683594 0-47.257813-33.738281-85.703125-75.207031-85.703125-41.464843 0-75.199219 38.445312-75.199219 85.703125 0 47.246094 33.734376 85.683594 75.199219 85.683594zm0 0" />
    <path d="m329.496094 192.4375h.003906c6.378906 2.117188 12.886719 3.128906 19.367188 3.128906 30.242187 0 59.714843-22.011718 70.960937-55.839844 6.476563-19.472656 6.050781-40.0625-1.199219-57.972656-7.585937-18.75-21.644531-32.359375-39.589844-38.324218-17.949218-5.964844-37.359374-3.476563-54.660156 7-16.527344 10.007812-29.191406 26.246093-35.660156 45.71875-13.652344 41.078124 4.640625 84.273437 40.777344 96.289062zm0 0" />
    <path d="m487.875 182.4375-.011719-.011719c-28.597656-21.125-71.367187-11.96875-95.347656 20.421875-23.957031 32.40625-20.210937 75.972656 8.34375 97.113282 10.414063 7.714843 22.71875 11.402343 35.3125 11.402343 21.949219 0 44.785156-11.203125 60.050781-31.804687 23.953125-32.40625 20.210938-75.972656-8.347656-97.121094zm0 0" />
  </svg>
);

export default function BlogList() {
  return (
    <div className="min-h-screen bg-white font-sans text-slate-600 relative overflow-x-hidden selection:bg-[#047857] selection:text-white">
      {/* Header unificado (fijo claro) */}
      <SiteHeader variant="solid" />

      {/* Hero */}
      <section className="relative bg-gradient-to-b from-[#047857]/[0.07] via-[#047857]/[0.03] to-white pt-32 sm:pt-40 pb-14 sm:pb-20 overflow-hidden">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[700px] h-[350px] bg-[#047857]/10 blur-[120px] rounded-full pointer-events-none"></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <span className="inline-flex items-center text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-5">
            Nuestro Blog <PawIcon className="w-5 h-5 ml-3" />
          </span>
          <h1 className="[font-family:Changa] text-4xl md:text-6xl lg:text-7xl font-extrabold tracking-tighter text-slate-900 uppercase leading-[1.05] mb-6">
            Blog de <span className="text-[#047857]">Salud Animal</span>
          </h1>
          <p className="text-base md:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Artículos clínicos, guías de cuidado y las últimas noticias en medicina veterinaria para mantener a tu compañero en su mejor estado.
          </p>
        </div>
      </section>

      {/* Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-32">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {blogData.map((post) => (
            <Link
              to={`/blog/${post.slug}`}
              key={post.id}
              className="group bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-md hover:shadow-xl hover:shadow-slate-200/60 hover:border-[#047857]/40 hover:-translate-y-1.5 transition-all duration-300 cursor-pointer flex flex-col"
            >
              <div className="h-56 overflow-hidden relative">
                <div className="absolute inset-0 bg-gradient-to-t from-slate-900/30 to-transparent z-10"></div>
                <img src={post.image} alt={post.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
              </div>
              <div className="p-6 sm:p-7 flex flex-col flex-grow">
                <div className="flex items-center text-xs font-bold text-[#047857] uppercase tracking-widest mb-3">
                  <BookOpen className="w-3 h-3 mr-1" /> Blog <span className="mx-2 text-slate-300">|</span> <Clock className="w-3 h-3 mr-1" /> {post.readTime}
                </div>
                <h3 className="text-lg font-bold text-slate-900 mb-2 leading-tight group-hover:text-[#065F46] transition-colors">{post.title}</h3>
                <p className="text-slate-500 text-sm leading-relaxed flex-grow">{post.shortDesc}</p>
                <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-xs text-slate-400">{post.date} • {post.author}</span>
                  <span className="inline-flex items-center text-[#047857] font-bold text-sm group-hover:text-[#065F46] transition-colors">
                    Leer más <ArrowRight className="w-4 h-4 ml-1 group-hover:translate-x-1 transition-transform" />
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
