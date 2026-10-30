import React, { useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Clock, Calendar as CalendarIcon, User, ArrowLeft, ArrowRight, BookOpen } from 'lucide-react';
import { blogData } from '../data/blogData';
import SiteHeader from '../components/SiteHeader';

export default function BlogPost() {
  const { slug } = useParams();
  const post = blogData.find(p => p.slug === slug);

  // Scroll to top on load
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  if (!post) {
    return (
      <div className="min-h-screen bg-white flex flex-col items-center justify-center text-slate-600 font-sans px-4 selection:bg-[#047857] selection:text-white">
        <SiteHeader variant="solid" />
        <div className="text-center">
          <BookOpen className="w-14 h-14 text-[#047857] mx-auto mb-6" />
          <h1 className="text-4xl font-bold mb-4 text-slate-900">Artículo no encontrado</h1>
          <Link to="/blog" className="inline-flex items-center text-[#047857] hover:text-[#065F46] font-bold">
            <ArrowLeft className="w-4 h-4 mr-2" /> Volver al Blog
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white font-sans text-slate-600 relative overflow-x-hidden selection:bg-[#047857] selection:text-white pb-32">
      {/* Header unificado (fijo claro) */}
      <SiteHeader variant="solid" />

      <div className="pt-32 sm:pt-40 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 relative animate-fade-in-up">

        {/* Volver al blog */}
        <Link to="/blog" className="inline-flex items-center text-[#047857] hover:text-[#065F46] font-bold text-sm mb-8">
          <ArrowLeft className="w-4 h-4 mr-2" /> Volver al Blog
        </Link>

        {/* Tarjeta del artículo */}
        <article className="bg-white rounded-3xl overflow-hidden shadow-lg shadow-slate-200/60 border border-slate-100">
          {/* Imagen destacada */}
          <div className="h-72 md:h-96 relative overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-t from-slate-900/40 to-transparent z-10"></div>
            <img src={post.image} alt={post.title} className="w-full h-full object-cover" />
          </div>

          <div className="p-8 md:p-12">
            {/* Meta Tags */}
            <div className="flex flex-wrap items-center gap-4 text-xs font-bold text-[#047857] uppercase tracking-widest mb-6">
              <span className="flex items-center"><CalendarIcon className="w-4 h-4 mr-2" /> {post.date}</span>
              <span className="text-slate-300">|</span>
              <span className="flex items-center"><Clock className="w-4 h-4 mr-2" /> {post.readTime}</span>
              <span className="text-slate-300">|</span>
              <span className="flex items-center"><User className="w-4 h-4 mr-2" /> {post.author}</span>
            </div>

            {/* Título */}
            <h1 className="text-3xl md:text-5xl font-extrabold text-slate-900 mb-10 leading-tight">
              {post.title}
            </h1>

            {/* Contenido */}
            <div className="prose prose-lg max-w-none prose-headings:text-slate-900 prose-p:text-slate-600 prose-a:text-[#047857] hover:prose-a:text-[#065F46] prose-strong:text-slate-900 prose-ul:text-slate-600">
              <div dangerouslySetInnerHTML={{ __html: post.content }} />
            </div>

            {/* Tags / Footer */}
            <div className="mt-12 pt-8 border-t border-slate-100 flex flex-wrap justify-between items-center gap-4">
              <div className="flex gap-2">
                <span className="bg-[#047857]/5 text-[#065F46] px-3 py-1 rounded-full text-xs font-bold border border-[#047857]/20">Medicina</span>
                <span className="bg-[#047857]/5 text-[#065F46] px-3 py-1 rounded-full text-xs font-bold border border-[#047857]/20">Cuidados</span>
              </div>
              <Link to="/blog" className="inline-flex items-center text-[#047857] hover:text-[#065F46] font-bold">
                Más artículos <ArrowRight className="w-4 h-4 ml-2" />
              </Link>
            </div>
          </div>
        </article>
      </div>
    </div>
  );
}
