import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Calendar, ChevronRight, Shield, Stethoscope, Syringe, Microscope, SprayCan, BookOpen, Clock, ArrowRight, Phone, MapPin, MessageCircle, Mail } from 'lucide-react';
import { blogData } from '../data/blogData';
import { useAuthStore } from '../store/authStore';
import api from '../api/axiosConfig';
import { getProductImage } from '../data/storeImages';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useGSAP } from '@gsap/react';
import SiteHeader from '../components/SiteHeader';

gsap.registerPlugin(ScrollTrigger);

// Hardcoded Data for Services and Blog
const servicesData = [
  {
    id: 's1',
    title: 'Consulta Integral',
    type: 'service',
    shortDesc: 'Evaluación médica completa y chequeos preventivos para mascotas de todas las edades.',
    icon: Stethoscope,
    color: 'text-cyan-400',
    shadow: 'drop-shadow-[0_0_15px_rgba(34,211,238,0.4)]',
    fullDesc: 'Nuestro servicio de consulta integral está diseñado para mantener a tu mascota en su estado óptimo. Empleamos protocolos de diagnóstico temprano y medicina preventiva, evaluando más de 20 puntos clave de salud.',
    benefits: [
      'Chequeo físico completo',
      'Evaluación nutricional',
      'Esquema de vacunación personalizado',
      'Detección temprana de patologías'
    ]
  },
  {
    id: 's2',
    title: 'Cirugía Especializada',
    type: 'service',
    shortDesc: 'Procedimientos quirúrgicos avanzados con monitoreo continuo y tecnología de punta.',
    icon: Syringe,
    color: 'text-pink-400',
    shadow: 'drop-shadow-[0_0_15px_rgba(244,114,182,0.4)]',
    fullDesc: 'Quirófano equipado con tecnología de monitorización cardiovascular en tiempo real. Contamos con cirujanos especialistas para procedimientos de tejidos blandos, traumatología y cirugía mínimamente invasiva.',
    benefits: [
      'Anestesia inhalatoria segura',
      'Monitoreo multiparamétrico',
      'Manejo del dolor post-quirúrgico',
      'Área de recuperación aislada'
    ]
  },
  {
    id: 's3',
    title: 'Diagnóstico y Laboratorio',
    type: 'service',
    shortDesc: 'Análisis rápidos y precisos para diagnósticos tempranos y seguimiento de tratamientos.',
    icon: Microscope,
    color: 'text-purple-400',
    shadow: 'drop-shadow-[0_0_15px_rgba(192,132,252,0.4)]',
    fullDesc: 'Laboratorio in-house que nos permite obtener resultados de analíticas en menos de 15 minutos, vital para emergencias. Además de rayos X digitales de alta resolución y ultrasonido.',
    benefits: [
      'Hemogramas y bioquímicas en tiempo real',
      'Radiografía digital de baja radiación',
      'Ecografía abdominal y ecocardiografía',
      'Test rápidos de enfermedades infecciosas'
    ]
  },
  {
    id: 's4',
    title: 'Peluquería y Bienestar',
    type: 'service',
    shortDesc: 'Servicios de aseo, higiene y cuidado de la piel para que tu mascota luzca y se sienta bien.',
    icon: SprayCan,
    color: 'text-blue-400',
    shadow: 'drop-shadow-[0_0_15px_rgba(96,165,250,0.4)]',
    fullDesc: 'Más que un corte de pelo, es una sesión de spa. Utilizamos productos dermatológicos de alta gama específicos para el pH y tipo de pelaje de cada raza, detectando posibles problemas cutáneos durante el proceso.',
    benefits: [
      'Baños terapéuticos y medicados',
      'Corte de raza estandarizado',
      'Limpieza profunda de oídos y glándulas',
      'Corte y limado de uñas sin estrés'
    ]
  }
];

// Diapositivas del banner principal
const heroSlides = [
  {
    image: '/slide2.jpg',
    tagline: 'Tu mascota merece el mejor cuidado.',
    titleLine1: 'Bienestar real para',
    titleLine2: 'tu mejor amigo,',
    titleLine3: 'en cada etapa de su vida.',
    cta: { label: 'Agendar Cita', to: '/login', icon: Calendar, contact: { icon: MapPin, text: 'Av. Los Andes 123, Lima' } }
  },
  {
    image: '/slide3.jpg',
    tagline: 'Amor y salud para toda tu familia.',
    titleLine1: 'Amor y salud',
    titleLine2: 'en un solo lugar,',
    titleLine3: 'para toda tu familia.',
    cta: { label: 'Contáctanos', to: 'mailto:contacto@sirbullpet.com', icon: MessageCircle, contact: { icon: Phone, text: '(+51) 999 999 999' } }
  }
];

// Equipo de expertos
const teamData = [
  { name: 'Daniel Logan', role: 'Veterinario', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_06.jpg' },
  { name: 'Layla Victoria', role: 'Veterinario', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_05.jpg' },
  { name: 'Cindey Harris', role: 'Veterinario', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_04.jpg' },
  { name: 'Lucas Tony', role: 'Cuidador de animales', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_03-1.jpg' },
  { name: 'Alissa Silva', role: 'Veterinario', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_02.jpg' },
  { name: 'Meghan Smith', role: 'Entrenador de mascotas', image: 'https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/team_01.jpg' }
];

const socialLinks = [
  { label: 'Twitter', path: 'M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z' },
  { label: 'Facebook', path: 'M13.5 9H16l.5-3h-3V4.5c0-.9.3-1.5 1.7-1.5H16.6V.2C16.3.2 15.2 0 14 0c-2.5 0-4 1.5-4 4.2V6H7v3h3v9h3.5V9z' },
  { label: 'Instagram', path: 'M12 2.2c3.2 0 3.6 0 4.9.1 1.2.1 1.8.2 2.2.4.6.2 1 .5 1.4.9.4.4.7.9.9 1.4.2.4.4 1 .4 2.2.1 1.3.1 1.7.1 4.9s0 3.6-.1 4.9c-.1 1.2-.2 1.8-.4 2.2-.2.6-.5 1-.9 1.4-.4.4-.9.7-1.4.9-.4.2-1 .4-2.2.4-1.3.1-1.7.1-4.9.1s-3.6 0-4.9-.1c-1.2-.1-1.8-.2-2.2-.4-.6-.2-1-.5-1.4-.9-.4-.4-.7-.9-.9-1.4-.2-.4-.4-1-.4-2.2-.1-1.3-.1-1.7-.1-4.9s0-3.6.1-4.9c.1-1.2.2-1.8.4-2.2.2-.6.5-1 .9-1.4.4-.4.9-.7 1.4-.9.4-.2 1-.4 2.2-.4 1.3-.1 1.7-.1 4.9-.1zM12 0C8.7 0 8.3 0 7 .1 5.7.2 4.8.4 4 .7 3.2 1 2.5 1.4 1.8 2.1 1.1 2.8.7 3.5.4 4.3.1 5.1 0 6 0 7.3 0 8.3 0 12s0 4.7.1 6c.1 1.3.3 2.2.6 3 .3.8.7 1.5 1.4 2.2.7.7 1.4 1.1 2.2 1.4.8.3 1.7.5 3 .6 1.3.1 1.7.1 6 .1s4.7 0 6-.1c1.3-.1 2.2-.3 3-.6.8-.3 1.5-.7 2.2-1.4.7-.7 1.1-1.4 1.4-2.2.3-.8.5-1.7.6-3 .1-1.3.1-1.7.1-6s0-4.7-.1-6c-.1-1.3-.3-2.2-.6-3-.3-.8-.7-1.5-1.4-2.2C20.5 1.1 19.8.7 19 .4c-.8-.3-1.7-.5-3-.6C14.7.2 14.3 0 12 0zm0 5.8c-3.4 0-6.2 2.8-6.2 6.2s2.8 6.2 6.2 6.2 6.2-2.8 6.2-6.2S15.4 5.8 12 5.8zm0 10.2c-2.2 0-4-1.8-4-4s1.8-4 4-4 4 1.8 4 4-1.8 4-4 4zm6.4-10.4c0-.8-.6-1.4-1.4-1.4s-1.4.6-1.4 1.4.6 1.4 1.4 1.4 1.4-.6 1.4-1.4z' }
];

const PawIcon = ({ className }) => (
  <svg viewBox="0 -32 512.00011 512" className={className} fill="currentColor">
    <path d="m342.382812 239.351562c-23.039062-35.941406-62.277343-57.402343-104.964843-57.402343s-81.925781 21.460937-104.960938 57.402343l-55.515625 86.605469c-9.210937 14.367188-13.460937 30.96875-12.292968 47.996094 1.167968 17.03125 7.648437 32.890625 18.738281 45.875 11.097656 12.972656 25.761719 21.839844 42.40625 25.644531 16.644531 3.804688 33.707031 2.179688 49.339843-4.691406l1.046876-.464844c39.335937-16.945312 84.285156-16.789062 123.519531.464844 10.121093 4.449219 20.84375 6.699219 31.664062 6.699219 5.882813 0 11.800781-.667969 17.664063-2.003907 16.644531-3.800781 31.308594-12.667968 42.410156-25.644531 11.09375-12.972656 17.578125-28.835937 18.75-45.871093 1.171875-17.035157-3.078125-33.632813-12.289062-48.007813zm0 0" />
    <path d="m91.894531 239.238281c16.515625-6.34375 29.0625-19.652343 35.328125-37.476562 5.964844-16.960938 5.476563-36.109375-1.378906-53.921875-6.859375-17.800782-19.335938-32.332032-35.132812-40.921875-16.59375-9.019531-34.824219-10.488281-51.3125-4.132813-33.171876 12.753906-48.394532 53.746094-33.929688 91.398438 11.554688 29.96875 38.503906 48.886718 65.75 48.886718 6.957031 0 13.933594-1.234374 20.675781-3.832031zm0 0" />
    <path d="m199.613281 171.386719c41.46875 0 75.207031-38.4375 75.207031-85.683594 0-47.257813-33.738281-85.703125-75.207031-85.703125-41.464843 0-75.199219 38.445312-75.199219 85.703125 0 47.246094 33.734376 85.683594 75.199219 85.683594zm0 0" />
    <path d="m329.496094 192.4375h.003906c6.378906 2.117188 12.886719 3.128906 19.367188 3.128906 30.242187 0 59.714843-22.011718 70.960937-55.839844 6.476563-19.472656 6.050781-40.0625-1.199219-57.972656-7.585937-18.75-21.644531-32.359375-39.589844-38.324218-17.949218-5.964844-37.359374-3.476563-54.660156 7-16.527344 10.007812-29.191406 26.246093-35.660156 45.71875-13.652344 41.078124 4.640625 84.273437 40.777344 96.289062zm0 0" />
    <path d="m487.875 182.4375-.011719-.011719c-28.597656-21.125-71.367187-11.96875-95.347656 20.421875-23.957031 32.40625-20.210937 75.972656 8.34375 97.113282 10.414063 7.714843 22.71875 11.402343 35.3125 11.402343 21.949219 0 44.785156-11.203125 60.050781-31.804687 23.953125-32.40625 20.210938-75.972656-8.347656-97.121094zm0 0" />
  </svg>
);

export default function Landing() {
  const [selectedItem, setSelectedItem] = useState(null); // Stores the full object of the selected Service or Blog
  const [slideIndex, setSlideIndex] = useState(0);
  const [productos, setProductos] = useState([]);
  const location = useLocation();
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const role = useAuthStore(state => state.role);

  const handleAgendarCita = () => {
    if (isAuthenticated && role === 'Cliente') {
      navigate('/portal');
    } else if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  };

  useEffect(() => {
    if (location.state?.scrollTo === 'nosotros') {
      document.getElementById('nosotros')?.scrollIntoView({ behavior: 'smooth' });
      navigate('/', { replace: true, state: null });
    }
  }, [location.state, navigate]);

  useEffect(() => {
    const fetchProductos = async () => {
      try {
        const res = await api.get('/api/productos');
        setProductos(res.data.filter(p => p.stock > 0));
      } catch (err) {
        // La sección de tienda muestra su estado vacío si no hay productos
      }
    };
    fetchProductos();
  }, []);

  const containerRef = React.useRef(null);
  const teamTrackRef = React.useRef(null);
  const [teamScroll, setTeamScroll] = useState({ left: false, right: true });

  const scrollTeam = (dir) => {
    const el = teamTrackRef.current;
    if (!el) return;
    el.scrollBy({ left: dir * el.clientWidth * 0.95, behavior: 'smooth' });
  };

  const updateTeamScroll = () => {
    const el = teamTrackRef.current;
    if (!el) return;
    const maxScroll = el.scrollWidth - el.clientWidth;
    setTeamScroll({ left: el.scrollLeft > 0, right: el.scrollLeft < maxScroll - 1 });
  };

  useEffect(() => {
    const id = setInterval(() => {
      setSlideIndex((i) => (i + 1) % heroSlides.length);
    }, 10000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    updateTeamScroll();
    window.addEventListener('resize', updateTeamScroll);
    return () => window.removeEventListener('resize', updateTeamScroll);
  }, []);

  useGSAP(() => {
    // Animación de los servicios
    gsap.fromTo('.service-card', 
      { opacity: 0, y: 50 },
      { 
        opacity: 1, 
        y: 0, 
        duration: 0.8, 
        stagger: 0.2,
        ease: 'power3.out',
        scrollTrigger: {
          trigger: '#servicios',
          start: 'top 80%',
        }
      }
    );
  }, { scope: containerRef });
  
  return (
    <div ref={containerRef} className="min-h-screen bg-[#06030F] font-sans text-slate-300 relative overflow-x-hidden selection:bg-cyan-500 selection:text-white">
      
      {/* Fondo del banner (slider) */}
      <div className="fixed inset-0 w-full h-full pointer-events-none z-0 overflow-hidden">
        {heroSlides.map((slide, i) => (
          <img
            key={i}
            src={slide.image}
            alt="Veterinaria Sirbull Pet"
            className={`absolute inset-0 w-full h-full object-cover object-center transition-opacity duration-[1500ms] ${i === slideIndex ? 'opacity-100' : 'opacity-0'}`}
          />
        ))}
        <div className="absolute inset-x-0 top-0 h-72 bg-gradient-to-b from-black/70 via-black/30 to-transparent"></div>
      </div>

      {/* Header unificado (overlay sobre el hero oscuro) */}
      <SiteHeader variant="overlay" showContactBar />

      {/* Hero Section */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 sm:px-16 lg:px-24 pt-40 sm:pt-48 lg:pt-64 mb-16 lg:mb-32">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          
          {/* Left Column: Copy & Buttons */}
          <div key={slideIndex} className="lg:col-span-12 animate-fade-in pr-10 sm:pr-0">
            <p className="text-sm sm:text-base font-semibold tracking-wide text-white mb-4">
              {heroSlides[slideIndex].tagline}
            </p>
            <h1 className="z-10 [font-family:Changa] text-left leading-tight lg:leading-[62px] font-bold text-4xl sm:text-5xl lg:text-[60px] w-full lg:w-[580px] mb-6 lg:mb-8 text-white min-h-[185px] sm:min-h-[240px] lg:min-h-[250px]">
              {heroSlides[slideIndex].titleLine1} {heroSlides[slideIndex].titleLine2} {heroSlides[slideIndex].titleLine3}
            </h1>
            
            {/* CTA + información de contacto por diapositiva */}
            {(() => {
              const cta = heroSlides[slideIndex].cta;
              const CtaIcon = cta.icon;
              const ContactIcon = cta.contact.icon;
              const isExternal = cta.to.startsWith('mailto:') || cta.to.startsWith('http');
              const isPrimary = cta.label === 'Agendar Cita';
              const btnClass = isPrimary
                ? "inline-flex items-center px-7 py-3.5 rounded-full bg-amber-500 text-slate-900 hover:bg-amber-400 shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all group"
                : "inline-flex items-center px-7 py-3.5 rounded-full bg-[#047857] text-white hover:bg-[#065F46] shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all group";
              const btnContent = (
                <>
                  <div className="flex items-center">
                    <CtaIcon className={`w-6 h-6 mr-3 ${isPrimary ? 'text-slate-900' : 'text-emerald-100'}`} />
                    <div className="text-left leading-tight mr-4">
                      <span className="block font-bold text-[14px] tracking-wide">{cta.label}</span>
                    </div>
                  </div>
                  <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                </>
              );
              return (
                <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 sm:gap-5">
                  {isExternal ? (
                    <a href={cta.to} className={btnClass}>{btnContent}</a>
                  ) : isPrimary ? (
                    <button type="button" onClick={handleAgendarCita} className={btnClass}>{btnContent}</button>
                  ) : (
                    <Link to={cta.to} className={btnClass}>{btnContent}</Link>
                  )}
                  <div className="flex items-center gap-2">
                    <ContactIcon className={`w-5 h-5 ${isPrimary ? 'text-amber-400' : 'text-emerald-300'} flex-shrink-0`} />
                    <span className="text-sm sm:text-base font-semibold text-white">{cta.contact.text}</span>
                  </div>
                </div>
              );
            })()}

          </div>

        </div>
      </div>

      {/* Centro de Peluquería: Calidad y experiencia */}
      <div className="bg-white w-full py-16 lg:py-24 relative z-20 shadow-[0_-20px_50px_rgba(0,0,0,0.5)]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 lg:gap-16 items-center">
            <div className="flex justify-center hover:-translate-y-2 transition-transform duration-500">
              <img
                src="https://demo.7iquid.com/pethund/wp-content/uploads/2020/07/services_cat.png"
                alt="Gato como paciente de la clínica veterinaria"
                className="w-full max-w-[440px] object-contain drop-shadow-2xl"
              />
            </div>

            <div>
              <span className="inline-flex items-center text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-4">
                Calidad y experiencia veterinaria
                <PawIcon className="w-5 h-5 ml-3" />
              </span>
              <h2 className="text-3xl md:text-5xl font-extrabold tracking-tighter uppercase leading-tight">
                <span className="text-[#047857]">Clínica veterinaria</span>{' '}
                <span className="text-slate-900">para el cuidado de tu mascota</span>
              </h2>
              <p className="text-slate-600 text-base md:text-lg leading-relaxed mt-6">
                En Sirbull Pet, la ambición es que siempre te sientas bienvenido. Contamos con médicos veterinarios
                calificados, equipos de diagnóstico de última generación y un trato cálido, para ofrecerle a tu mascota
                una atención integral en salud, prevención y bienestar durante toda su vida.
              </p>

              <ul className="mt-8 space-y-4">
                {[
                  'Consultas y chequeos médicos integrales',
                  'Vacunación, desparasitación y análisis de laboratorio',
                  'Cirugías y urgencias con monitoreo continuo'
                ].map((item) => (
                  <li key={item} className="flex items-start gap-4">
                    <span className="w-11 h-11 flex-shrink-0 rounded-xl bg-[#047857]/10 text-[#047857] flex items-center justify-center">
                      <PawIcon className="w-6 h-6" />
                    </span>
                    <span className="text-slate-700 font-medium leading-relaxed pt-1.5">{item}</span>
                  </li>
                ))}
              </ul>

              <div className="mt-10">
                <a href="#servicios" className="inline-flex items-center px-9 py-3.5 rounded-full bg-[#047857] text-white font-bold text-sm uppercase tracking-widest shadow-md hover:bg-[#065F46] hover:shadow-lg hover:-translate-y-0.5 transition-all">
                  Conoce nuestros servicios <ChevronRight className="w-4 h-4 ml-2" />
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Team Section */}
      <div id="equipo" className="relative z-10 bg-gradient-to-b from-slate-50 to-white py-20 border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-14">
            <span className="inline-block text-sm font-bold text-slate-900 uppercase tracking-[0.2em] mb-4">Personal de cuidado de mascotas</span>
            <h2 className="text-3xl md:text-5xl font-extrabold tracking-tighter uppercase">
              <span className="text-[#047857]">Equipo de</span>{' '}
              <span className="text-slate-900">Especialistas Veterinarios</span>
            </h2>
            <p className="text-slate-600 max-w-2xl mx-auto mt-4 text-base md:text-lg">
              Conozca a nuestros profesionales. Contamos con un equipo experimentado y cualificado para cuidar de tu mejor amigo.
            </p>
          </div>

          <div className="max-w-5xl mx-auto">
          <div className="relative">
            <div
              ref={teamTrackRef}
              onScroll={updateTeamScroll}
              className="flex overflow-x-auto gap-6 pb-4 scroll-smooth snap-x snap-mandatory team-scrollbar"
            >
              {teamData.map((member) => (
                <div
                  key={member.name}
                  className="group bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-md hover:shadow-xl hover:-translate-y-1.5 transition-all duration-300 w-full sm:w-[calc(50%-0.75rem)] lg:w-[calc(33.333%-1rem)] flex-shrink-0 snap-start"
                >
                  <div className="p-4">
                    <div className="relative h-56 overflow-hidden rounded-xl">
                      <img src={member.image} alt={member.name} className="w-full h-full object-cover object-top group-hover:scale-105 transition-transform duration-500" />
                      <div className="absolute inset-x-0 bottom-0 flex items-center justify-center gap-3 bg-gradient-to-t from-slate-900/80 to-transparent pt-12 pb-4 translate-y-full group-hover:translate-y-0 transition-transform duration-300">
                        {socialLinks.map((social) => (
                          <a
                            key={social.label}
                            href="#"
                            aria-label={social.label}
                            className="w-9 h-9 rounded-full bg-white/15 backdrop-blur-sm border border-white/30 flex items-center justify-center text-white hover:bg-[#0EA5E9] hover:border-[#0EA5E9] transition-colors"
                          >
                            <svg viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4">
                              <path d={social.path} />
                            </svg>
                          </a>
                        ))}
                      </div>
                    </div>
                  </div>
                  <div className="px-5 pb-5 text-center">
                    <h3 className="text-base font-bold text-[#047857]">{member.name}</h3>
                    <p className="text-sm font-medium text-slate-700 mt-1">{member.role}</p>
                    <span className="inline-flex items-center text-xs font-bold text-slate-400 uppercase tracking-widest mt-3 group-hover:text-[#0EA5E9] transition-colors">
                    Leer más <ChevronRight className="w-3 h-3 ml-1" />
                  </span>
                </div>
                </div>
              ))}
            </div>

            <button
              onClick={() => scrollTeam(-1)}
              disabled={!teamScroll.left}
              aria-label="Anterior"
              className="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1/2 w-11 h-11 rounded-full bg-[#047857] border-2 border-[#047857] shadow-lg text-white flex items-center justify-center hover:bg-[#065F46] hover:border-[#065F46] transition-all disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-[#047857] disabled:hover:border-[#047857]"
            >
              <ChevronRight className="w-5 h-5 rotate-180" />
            </button>
            <button
              onClick={() => scrollTeam(1)}
              disabled={!teamScroll.right}
              aria-label="Siguiente"
              className="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2 w-11 h-11 rounded-full bg-[#047857] border-2 border-[#047857] shadow-lg text-white flex items-center justify-center hover:bg-[#065F46] hover:border-[#065F46] transition-all disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-[#047857] disabled:hover:border-[#047857]"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>
          </div>

          <div className="text-center mt-14">
            <button className="inline-flex items-center px-10 py-3.5 rounded-full border-2 border-[#0EA5E9] text-[#0EA5E9] font-bold text-sm uppercase tracking-widest hover:bg-[#0EA5E9] hover:text-white transition-colors">
              Ver todo el equipo
            </button>
          </div>
        </div>
      </div>

      {/* Services Section */}
      <section id="servicios" className="relative z-10 bg-white py-20 lg:py-24 border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-12 lg:mb-16">
            <span className="inline-flex items-center text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-4">
              Servicios de Vanguardia <PawIcon className="w-5 h-5 ml-3" />
            </span>
            <h2 className="text-3xl md:text-5xl font-extrabold tracking-tighter uppercase">
              <span className="text-[#047857]">Nuestros Servicios</span>{' '}
              <span className="text-slate-900">de Vanguardia</span>
            </h2>
            <p className="text-slate-600 text-base md:text-lg mt-4 max-w-xl">
              Atención médica especializada, tecnología de diagnóstico y bienestar integral para tu mascota.
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {servicesData.map((service) => {
              const Icon = service.icon;
              return (
                <div 
                  key={service.id} 
                  onClick={() => setSelectedItem(service)}
                  className="service-card bg-white border border-slate-100 rounded-2xl shadow-md hover:shadow-xl hover:shadow-slate-200/60 hover:border-[#047857]/40 hover:-translate-y-1.5 transition-all duration-300 cursor-pointer p-8 flex items-start group"
                >
                  <div className="flex-shrink-0 mr-6">
                    <Icon className={`w-12 h-12 ${service.color} group-hover:scale-110 transition-transform ${service.shadow}`} strokeWidth={1.5} />
                  </div>
                  <div>
                    <h3 className="text-xl font-bold text-slate-900 mb-2 group-hover:text-[#065F46] transition-colors">{service.title}</h3>
                    <p className="text-slate-500 text-sm leading-relaxed mb-4">
                      {service.shortDesc}
                    </p>
                    <span className="text-xs font-bold text-[#047857] uppercase tracking-widest flex items-center group-hover:translate-x-2 transition-transform">
                      Ver Detalles <ArrowRight className="w-3 h-3 ml-1" />
                    </span>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </section>

      {/* Tienda Section */}
      <section id="tienda" className="relative z-10 bg-white py-20 lg:py-24 border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12 lg:mb-16">
            <div>
              <span className="inline-flex items-center text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-4">
                Tienda Oficial <PawIcon className="w-5 h-5 ml-3" />
              </span>
              <h2 className="text-3xl md:text-5xl font-extrabold tracking-tighter uppercase">
                <span className="text-[#047857]">Productos</span>{' '}
                <span className="text-slate-900">destacados</span>
              </h2>
              <p className="text-slate-600 text-base md:text-lg mt-4 max-w-xl">
                Alimentos, cuidados y accesorios seleccionados por nuestros veterinarios para la salud integral de tu mascota.
              </p>
            </div>
            <Link to="/tienda" className="inline-flex items-center px-8 py-3.5 rounded-full bg-[#047857] text-white font-bold text-sm uppercase tracking-widest shadow-md hover:bg-[#065F46] hover:shadow-lg hover:-translate-y-0.5 transition-all self-start md:self-auto">
              Ver tienda <ChevronRight className="w-4 h-4 ml-2" />
            </Link>
          </div>

          {productos.length > 0 ? (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              {productos.slice(0, 4).map((p) => (
                <Link to="/tienda" key={p.id} className="group bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm hover:shadow-lg hover:shadow-slate-200/60 hover:-translate-y-1 transition-all duration-300">
                  <div className="aspect-square bg-slate-50 overflow-hidden flex items-center justify-center p-6">
                    <img src={p.imagen || getProductImage(p.nombre)} alt={p.nombre} className="max-h-full max-w-full object-contain group-hover:scale-110 transition-transform duration-500" />
                  </div>
                  <div className="p-4">
                    <h4 className="text-sm font-bold text-slate-900 leading-snug line-clamp-2 min-h-[2.5rem] group-hover:text-[#065F46] transition-colors">{p.nombre}</h4>
                    <div className="mt-2 flex items-center justify-between">
                      <span className="text-lg font-extrabold text-slate-900">
                        <span className="text-xs align-top text-slate-400 mr-0.5">$</span>{p.precio.toFixed(2)}
                      </span>
                      <span className="inline-flex items-center text-xs font-bold text-[#047857] uppercase tracking-widest">
                        Ver <ChevronRight className="w-3 h-3 ml-1 group-hover:translate-x-1 transition-transform" />
                      </span>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="bg-slate-50 border border-slate-200 rounded-2xl p-10 text-center">
              <p className="text-slate-500">Los productos se cargarán aquí. Visita la tienda para ver el catálogo completo.</p>
              <Link to="/tienda" className="mt-4 inline-flex items-center text-[#047857] font-bold">
                Ir a la tienda <ChevronRight className="w-4 h-4 ml-2" />
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* Blog Section */}
      <section id="blog" className="relative z-10 bg-white py-20 lg:py-24 border-t border-slate-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12 lg:mb-16">
            <div>
              <span className="inline-flex items-center text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-4">
                Nuestro Blog <PawIcon className="w-5 h-5 ml-3" />
              </span>
              <h2 className="text-3xl md:text-5xl font-extrabold tracking-tighter uppercase">
                <span className="text-[#047857]">Últimos</span>{' '}
                <span className="text-slate-900">Artículos</span>
              </h2>
              <p className="text-slate-600 text-base md:text-lg mt-4 max-w-xl">
                Consejos, cuidados y novedades para mantener a tu mascota sana y feliz.
              </p>
            </div>
            <Link to="/blog" className="hidden md:flex items-center text-[#047857] hover:text-[#065F46] font-bold transition-colors">
              Ver todo el blog <ArrowRight className="w-5 h-5 ml-2" />
            </Link>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {blogData.slice(0,3).map((post) => (
              <Link 
                to={`/blog/${post.slug}`}
                key={post.id}
                className="group bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-md hover:shadow-xl hover:shadow-slate-200/60 hover:border-[#047857]/40 hover:-translate-y-1.5 transition-all duration-300 cursor-pointer flex flex-col"
              >
                <div className="h-48 overflow-hidden relative">
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-900/30 to-transparent z-10"></div>
                  <img src={post.image} alt={post.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
                </div>
                <div className="p-6 flex flex-col flex-grow">
                  <div className="flex items-center text-xs font-bold text-[#047857] uppercase tracking-widest mb-3">
                    <BookOpen className="w-3 h-3 mr-1" /> Blog <span className="mx-2 text-slate-300">|</span> <Clock className="w-3 h-3 mr-1" /> {post.readTime}
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mb-2 leading-tight group-hover:text-[#065F46] transition-colors">{post.title}</h3>
                  <p className="text-slate-500 text-sm leading-relaxed flex-grow">
                    {post.shortDesc}
                  </p>
                </div>
              </Link>
            ))}
          </div>
          <div className="mt-10 text-center md:hidden">
            <Link to="/blog" className="inline-flex items-center text-[#047857] hover:text-[#065F46] font-bold transition-colors">
              Ver todo el blog <ArrowRight className="w-5 h-5 ml-2" />
            </Link>
          </div>
        </div>
      </section>

      {/* Testimonials & Footer */}
      <div id="nosotros" className="relative z-20 bg-white">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row">
          <div className="w-full md:w-1/2">
            <img src="https://images.unsplash.com/photo-1576201836106-db1758fd1c97?auto=format&fit=crop&q=80&w=800" alt="Veterinarian with client" className="w-full h-full object-cover min-h-[400px]" />
          </div>
          <div className="w-full md:w-1/2 p-12 md:p-24 flex flex-col justify-center bg-gradient-to-br from-white to-slate-50">
            <h3 className="text-3xl font-black text-slate-900 mb-8 uppercase tracking-tighter">Testimonios de Clientes</h3>
            <blockquote className="text-xl italic text-slate-600 mb-6 border-l-4 border-cyan-500 pl-6">
              "¡Excelente servicio! Sirbull Pet ha facilitado la gestión de la salud de mi perro. Rápido y confiable."
            </blockquote>
            <p className="font-bold text-slate-900">- María Gómez</p>
          </div>
        </div>
        
        {/* Footer */}
        <footer className="bg-gradient-to-b from-white to-slate-50 border-t border-slate-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 lg:py-16">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10 lg:gap-8">

              {/* Marca */}
              <div>
                <Link to="/" className="inline-block">
                  <img src="/logo-dark.png" alt="Sirbull Pet" className="h-12 w-auto object-contain" />
                </Link>
                <p className="text-slate-500 text-sm leading-relaxed mt-5 max-w-xs">
                  Cuidamos de la salud y el bienestar de tu mascota con profesionales certificados y tecnología de vanguardia.
                </p>
                <div className="flex items-center gap-3 mt-6">
                  {socialLinks.map((social) => (
                    <a key={social.label} href="#" aria-label={social.label} className="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:bg-[#047857] hover:border-[#047857] hover:text-white shadow-sm transition-colors">
                      <svg viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4">
                        <path d={social.path} />
                      </svg>
                    </a>
                  ))}
                </div>
              </div>

              {/* Enlaces */}
              <div>
                <h4 className="text-sm font-bold text-slate-900 uppercase tracking-widest mb-5">Enlaces</h4>
                <ul className="space-y-3">
                  <li><Link to="/" className="text-slate-500 hover:text-[#065F46] transition-colors text-sm">Inicio</Link></li>
                  <li><Link to="/tienda" className="text-slate-500 hover:text-[#065F46] transition-colors text-sm">Tienda</Link></li>
                  <li><a href="#servicios" className="text-slate-500 hover:text-[#065F46] transition-colors text-sm">Servicios</a></li>
                  <li><a href="#blog" className="text-slate-500 hover:text-[#065F46] transition-colors text-sm">Blog</a></li>
                  <li><a href="#nosotros" className="text-slate-500 hover:text-[#065F46] transition-colors text-sm">Sobre Nosotros</a></li>
                </ul>
              </div>

              {/* Contacto */}
              <div>
                <h4 className="text-sm font-bold text-slate-900 uppercase tracking-widest mb-5">Contacto</h4>
                <ul className="space-y-4">
                  <li className="flex items-start gap-3 text-slate-500 text-sm">
                    <MapPin className="w-5 h-5 text-[#047857] flex-shrink-0 mt-0.5" />
                    Pet Street 123 - Nueva York
                  </li>
                  <li className="flex items-center gap-3 text-slate-500 text-sm">
                    <Phone className="w-5 h-5 text-[#047857] flex-shrink-0" />
                    (123) 456-789
                  </li>
                  <li className="flex items-center gap-3 text-slate-500 text-sm">
                    <Mail className="w-5 h-5 text-[#047857] flex-shrink-0" />
                    contacto@sirbullpet.com
                  </li>
                </ul>
              </div>

              {/* Horario */}
              <div>
                <h4 className="text-sm font-bold text-slate-900 uppercase tracking-widest mb-5">Horario</h4>
                <ul className="space-y-3 text-sm">
                  <li className="flex items-center justify-between text-slate-500">
                    <span>Lunes - Viernes</span>
                    <span className="font-semibold text-slate-700">9:00 - 18:00</span>
                  </li>
                  <li className="flex items-center justify-between text-slate-500">
                    <span>Sábado</span>
                    <span className="font-semibold text-slate-700">9:00 - 13:00</span>
                  </li>
                  <li className="flex items-center justify-between text-slate-500">
                    <span>Domingo</span>
                    <span className="font-semibold text-slate-700">Cerrado</span>
                  </li>
                </ul>
              </div>

            </div>
          </div>

          {/* Barra inferior */}
          <div className="border-t border-slate-200 bg-white py-6">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-3">
              <p className="text-slate-500 text-sm">© 2026 SIRBULL PET - Todos los derechos reservados.</p>
              <div className="flex items-center gap-6 text-sm">
                <a href="#" className="text-slate-500 hover:text-[#065F46] transition-colors">Política de Privacidad</a>
                <a href="#" className="text-slate-500 hover:text-[#065F46] transition-colors">Términos y Condiciones</a>
              </div>
            </div>
          </div>
        </footer>
      </div>

      {/* Expandable Info Modal (Phase 15) */}
      {selectedItem && <ExpandableModal item={selectedItem} onClose={() => setSelectedItem(null)} />}
    </div>
  );
}

// Reusable Expandable Modal for Services and Blog
function ExpandableModal({ item, onClose }) {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const role = useAuthStore(state => state.role);

  if (!item) return null;
  const isService = item.type === 'service';
  const Icon = item.icon || null;

  const handleProgramarCita = () => {
    onClose();
    if (isAuthenticated && role === 'Cliente') {
      navigate('/portal');
    } else if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 sm:p-6">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-slate-900/60 backdrop-blur-md animate-fade-in-up" 
        style={{ animationDuration: '0.3s' }}
        onClick={onClose}
      ></div>

      {/* Card Content */}
      <div className="relative w-full max-w-2xl bg-white border border-slate-200 rounded-3xl shadow-[0_25px_80px_rgba(0,0,0,0.25)] overflow-hidden animate-modal-bounce z-10 flex flex-col max-h-[90vh]">
        
        {/* Close button */}
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 w-10 h-10 bg-slate-100 hover:bg-slate-200 rounded-full flex items-center justify-center text-slate-500 hover:text-slate-900 transition-colors z-20 backdrop-blur-sm"
        >
          &times;
        </button>

        {isService ? (
          /* Service Modal Content */
          <div className="p-8 sm:p-12 overflow-y-auto">
            <div className="flex items-center mb-6">
              <div className="w-16 h-16 rounded-2xl bg-[#047857]/10 border border-[#047857]/30 flex items-center justify-center mr-6 shadow-inner">
                <Icon className={`w-8 h-8 ${item.color}`} strokeWidth={2} />
              </div>
              <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">{item.title}</h2>
            </div>
            
            <p className="text-lg text-slate-600 leading-relaxed mb-8">
              {item.fullDesc}
            </p>

            <h4 className="text-xs font-bold text-[#047857] uppercase tracking-widest mb-4">Beneficios Incluidos</h4>
            <ul className="space-y-3 mb-10">
              {item.benefits.map((benefit, i) => (
                <li key={i} className="flex items-center text-slate-600">
                  <Shield className="w-4 h-4 text-[#047857] mr-3 flex-shrink-0" />
                  {benefit}
                </li>
              ))}
            </ul>

            <button onClick={handleProgramarCita} className="w-full py-4 rounded-xl bg-amber-500 text-slate-900 font-bold hover:bg-amber-400 transition-colors shadow-[0_10px_30px_rgba(245,158,11,0.3)]">
              Cerrar y Programar Cita
            </button>
          </div>
        ) : (
          /* Blog Modal Content */
          <div className="flex flex-col h-full">
            <div className="h-64 sm:h-80 w-full relative flex-shrink-0">
              <div className="absolute inset-0 bg-gradient-to-t from-white to-transparent z-10"></div>
              <img src={item.image} alt={item.title} className="w-full h-full object-cover" />
            </div>
            <div className="p-8 sm:p-12 -mt-20 relative z-20 overflow-y-auto">
              <div className="flex items-center text-xs font-bold text-[#047857] uppercase tracking-widest mb-3 bg-white/90 border border-slate-200 w-fit px-3 py-1 rounded-full shadow-sm">
                <BookOpen className="w-3 h-3 mr-1" /> Artículo <span className="mx-2 text-slate-300">|</span> <Clock className="w-3 h-3 mr-1" /> {item.readTime}
              </div>
              <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mb-6 leading-tight">{item.title}</h2>
              <div className="text-slate-600 leading-relaxed whitespace-pre-wrap">
                {item.fullDesc}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}


