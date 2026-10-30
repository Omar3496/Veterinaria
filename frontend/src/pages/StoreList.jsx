import React, { useState, useEffect } from 'react';
import { useToast } from '../components/ui/ToastProvider';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Plus, Minus, Trash2, CreditCard, AlertCircle, CheckCircle2, PackageSearch, Shield, SlidersHorizontal, ChevronDown, X, Star, Heart, GitCompareArrows, ChevronRight, MessageCircle, Phone, Truck, Banknote, Smartphone, Store, Lock } from 'lucide-react';
import api from '../api/axiosConfig';
import { useAuthStore } from '../store/authStore';
import { getProductImage } from '../data/storeImages';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import SiteHeader from '../components/SiteHeader';

// ─── Categorías derivadas del nombre del producto (adaptadas del shop demo) ───
const CATEGORIES = [
  {
    id: 'alimento-perros',
    label: 'Alimento para Perros',
    children: [
      { id: 'alimento-perros-seco', label: 'Comida Seca', keywords: ['croqueta', 'pienso', 'seco', 'kg', 'dogchow', 'pedigree', 'dog'] },
      { id: 'alimento-perros-humedo', label: 'Comida Húmeda', keywords: ['humeda', 'húmeda', 'lata', 'sobre', 'wet'] },
    ],
  },
  {
    id: 'alimento-gatos',
    label: 'Alimento para Gatos',
    keywords: ['gato', 'whiskas', 'cat'],
  },
  {
    id: 'golosinas',
    label: 'Golosinas y Snacks',
    keywords: ['golosina', 'snack', 'premio', 'bocadito', 'galleta'],
  },
  {
    id: 'higiene',
    label: 'Higiene y Aseo',
    children: [
      { id: 'higiene-champu', label: 'Champús y Jabones', keywords: ['champu', 'champú', 'jabon', 'shampoo', 'shampú'] },
      { id: 'higiene-cepillos', label: 'Cepillos y Peines', keywords: ['cepillo', 'peine'] },
    ],
  },
  {
    id: 'farmacia',
    label: 'Farmacia',
    children: [
      { id: 'farmacia-vacunas', label: 'Vacunas', keywords: ['vacuna'] },
      { id: 'farmacia-medicamentos', label: 'Medicamentos', keywords: ['medicamento', 'antibiotico', 'antibiótico', 'pastilla', 'jeringa', 'analgesico'] },
      { id: 'farmacia-desparasitantes', label: 'Desparasitantes', keywords: ['desparasitante', 'antipulgas', 'anti pulgas', 'pulgas', 'garrapatas'] },
    ],
  },
  {
    id: 'juguetes',
    label: 'Juguetes',
    children: [
      { id: 'juguetes-pelotas', label: 'Pelotas', keywords: ['pelota'] },
      { id: 'juguetes-cuerdas', label: 'Cuerdas y Huesos', keywords: ['cuerda', 'hueso', 'mordedor', 'juguete'] },
    ],
  },
  {
    id: 'accesorios',
    label: 'Accesorios',
    children: [
      { id: 'accesorios-correas', label: 'Correas y Collares', keywords: ['correa', 'collar', 'arnes', 'arnés'] },
      { id: 'accesorios-camas', label: 'Camas y Mantas', keywords: ['cama', 'manta', 'colchon', 'colchón'] },
    ],
  },
];

// Marcas reconocibles por nombre; si no se detecta, se usa el proveedor
const KNOWN_BRANDS = [
  'DogChow', 'Purina', 'Pro Plan', 'Royal Canin', "Hill's", 'Pedigree', 'Whiskas',
  'Optimum', 'Advance', 'Eukanuba', 'Kong', 'Pet Line', 'Vetflax', 'NexGard',
  'Advantage', 'Frontline', 'Bravecto', 'Canina',
];

const getCategoriaId = (nombre) => {
  const n = (nombre || '').toLowerCase();
  for (const cat of CATEGORIES) {
    if (cat.children) {
      for (const sub of cat.children) {
        if (sub.keywords.some(k => n.includes(k))) return sub.id;
      }
    } else if (cat.keywords) {
      if (cat.keywords.some(k => n.includes(k))) return cat.id;
    }
  }
  return 'otros';
};

const getCategoriaLabel = (id) => {
  for (const cat of CATEGORIES) {
    if (cat.id === id) return cat.label;
    if (cat.children) {
      const sub = cat.children.find(c => c.id === id);
      if (sub) return sub.label;
    }
  }
  return 'Otros productos';
};

const getMarca = (prod) => {
  const n = (prod.nombre || '').toLowerCase();
  const known = KNOWN_BRANDS.find(b => n.includes(b.toLowerCase()));
  if (known) return known;
  return prod.proveedor_nombre || 'Genérico';
};

// Calificación y reseñas pseudo-estables (basadas en el id para que no cambien al filtrar)
const ratingFor = (id) => 4 + ((id * 7) % 10) / 10;
const reviewsFor = (id) => 1 + ((id * 13) % 50);

const Stars = ({ rating }) => {
  const pct = Math.max(0, Math.min(100, (rating / 5) * 100));
  return (
    <div className="relative inline-block leading-none">
      <div className="flex">
        {[0, 1, 2, 3, 4].map(i => (
          <Star key={i} className="w-4 h-4 text-gray-300 fill-gray-300" />
        ))}
      </div>
      <div className="absolute inset-0 overflow-hidden" style={{ width: `${pct}%` }}>
        <div className="flex">
          {[0, 1, 2, 3, 4].map(i => (
            <Star key={i} className="w-4 h-4 text-amber-400 fill-amber-400" />
          ))}
        </div>
      </div>
    </div>
  );
};

// Diapositivas del banner de la tienda
const storeHeroSlides = [
  {
    image: '/slideshop1.jpg',
    tagline: 'Todo para tu mascota, en un solo lugar.',
    titleLine1: 'Encuentra todo',
    titleLine2: 'para tu mascota,',
    titleLine3: 'con la mejor calidad.',
    cta: { label: 'Explorar Tienda', to: '#tienda', icon: ShoppingCart, contact: { icon: Truck, text: 'Envíos a todo el país' } }
  },
  {
    image: '/slideshop2.jpg',
    tagline: 'Marcas y ofertas que tu mascota ama.',
    titleLine1: 'Las mejores marcas',
    titleLine2: 'y las mejores ofertas',
    titleLine3: 'para tu mascota.',
    cta: { label: 'Ver Ofertas', to: '#tienda', icon: MessageCircle, contact: { icon: Phone, text: 'Atención al cliente (+51) 999 999 999' } }
  }
];

export default function StoreList() {
  const toast = useToast();
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Cart state
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('sirbull_cart');
    return saved ? JSON.parse(saved) : [];
  });
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [showCheckout, setShowCheckout] = useState(false);
  const [tipoComprobante, setTipoComprobante] = useState('Boleta');
  const [tipoDocumento, setTipoDocumento] = useState('DNI');
  const [documentoFiscal, setDocumentoFiscal] = useState('');
  const [metodoPago, setMetodoPago] = useState('Efectivo');
  const [ventaConfirmada, setVentaConfirmada] = useState(null);
  const [checkoutStatus, setCheckoutStatus] = useState(null); 
  const [checkoutError, setCheckoutError] = useState('');

  const maxDigitos = { DNI: 8, RUC: 11, CE: 12 }
  const tipoDocLabel = (doc, comprobante) => {
    const d = (doc || '').trim()
    if (comprobante === 'Factura') return 'RUC'
    if (d.length === 12) return 'CE'
    return 'DNI'
  }

  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showAuthAlert, setShowAuthAlert] = useState(false);

  // Filters state
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [priceRange, setPriceRange] = useState(null);
  const [selectedBrands, setSelectedBrands] = useState([]);
  const [expandedCats, setExpandedCats] = useState({});
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Hero state
  const [slideIndex, setSlideIndex] = useState(0);

  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProductos = async () => {
      try {
        const res = await api.get('/api/productos');
        setProductos(res.data);
      } catch (err) {
        setError("No se pudieron cargar los productos en este momento.");
        toast.error("No se pudieron cargar los productos en este momento.");
      } finally {
        setLoading(false);
      }
    };
    fetchProductos();
  }, []);

  useEffect(() => {
    localStorage.setItem('sirbull_cart', JSON.stringify(cart));
  }, [cart]);

  // Auto-advance del hero
  useEffect(() => {
    const id = setInterval(() => {
      setSlideIndex((i) => (i + 1) % storeHeroSlides.length);
    }, 10000);
    return () => clearInterval(id);
  }, []);

  // Inicializar rango de precio con el mínimo/máximo de los productos disponibles
  useEffect(() => {
    const disponibles = productos.filter(p => p.stock > 0);
    if (disponibles.length && !priceRange) {
      const prices = disponibles.map(p => p.precio);
      setPriceRange({
        min: Math.floor(Math.min(...prices)),
        max: Math.ceil(Math.max(...prices)),
      });
    }
  }, [productos]);

  const addToCart = (prod) => {
    if (!isAuthenticated) {
      setShowAuthAlert(true);
      return;
    }
    const exists = cart.find(item => item.id === prod.id);
    if (exists) {
      if (exists.cantidad >= prod.stock) {
        toast.info("Stock máximo alcanzado.");
        return;
      }
      setCart(cart.map(item => item.id === prod.id ? { ...item, cantidad: item.cantidad + 1 } : item));
    } else {
      setCart([...cart, { ...prod, cantidad: 1 }]);
    }
    setIsCartOpen(true);
  };

  const updateQty = (id, delta) => {
    setCart(cart.map(item => {
      if (item.id === id) {
        const newQty = item.cantidad + delta;
        if (newQty > 0 && newQty <= item.stock) return { ...item, cantidad: newQty };
      }
      return item;
    }));
  };

  const removeFromCart = (id) => setCart(cart.filter(item => item.id !== id));
  const cartTotal = cart.reduce((acc, item) => acc + (item.precio * item.cantidad), 0);
  const cartItemsCount = cart.reduce((acc, item) => acc + item.cantidad, 0);

  const handleCheckout = async () => {
    if (!isAuthenticated) {
      setShowAuthAlert(true);
      return;
    }
    if (cart.length === 0) return;

    const doc = documentoFiscal.trim();
    if (tipoDocumento === 'RUC' && !/^\d{11}$/.test(doc)) {
      setCheckoutError('Ingresa un RUC válido de 11 dígitos para emitir Factura');
      return;
    }
    if (tipoDocumento === 'DNI' && !/^\d{8}$/.test(doc)) {
      setCheckoutError('Ingresa un DNI válido de 8 dígitos para emitir Boleta');
      return;
    }
    if (tipoDocumento === 'CE' && !/^20\d{10}$/.test(doc)) {
      setCheckoutError('Ingresa un Carnet de Extranjería válido (12 dígitos, inicia con 20)');
      return;
    }

    setCheckoutStatus('loading');
    setCheckoutError('');

    try {
      const payload = {
        metodo_pago: metodoPago,
        tipo_comprobante: tipoComprobante,
        documento_fiscal: doc,
        detalles: cart.map(c => ({
          producto_id: c.id,
          cantidad: c.cantidad,
          precio_unitario: c.precio
        }))
      };

      const res = await api.post('/api/ventas', payload);
      setCheckoutStatus('success');
      setVentaConfirmada(res.data);
      setCart([]);
      setIsCartOpen(false);

    } catch (err) {
      const msg = err?.response?.data?.detail;
      setCheckoutStatus('error');
      setCheckoutError(typeof msg === 'string' ? msg : "Error al procesar la compra. Revisa tu conexión o contacta soporte.");
    }
  };

  const openCheckout = () => {
    if (!isAuthenticated) {
      setShowAuthAlert(true);
      return;
    }
    if (cart.length === 0) return;
    setCheckoutStatus(null);
    setCheckoutError('');
    setDocumentoFiscal('');
    setTipoDocumento('DNI');
    setShowCheckout(true);
    setIsCartOpen(false);
  };

  // ─── Filtros ───
  const availableProducts = productos.filter(p => p.stock > 0);
  const globalMin = availableProducts.length ? Math.floor(Math.min(...availableProducts.map(p => p.precio))) : 0;
  const globalMax = availableProducts.length ? Math.ceil(Math.max(...availableProducts.map(p => p.precio))) : 100;
  const bounds = priceRange || { min: globalMin, max: globalMax };

  const categoryCounts = {};
  availableProducts.forEach(p => {
    const id = getCategoriaId(p.nombre);
    categoryCounts[id] = (categoryCounts[id] || 0) + 1;
  });

  const brandList = [...new Set(availableProducts.map(p => getMarca(p)))].sort((a, b) => a.localeCompare(b));

  const filtered = availableProducts.filter(p => {
    if (selectedCategory && getCategoriaId(p.nombre) !== selectedCategory) return false;
    if (p.precio < bounds.min || p.precio > bounds.max) return false;
    if (selectedBrands.length && !selectedBrands.includes(getMarca(p))) return false;
    return true;
  });

  const toggleBrand = (brand) => {
    setSelectedBrands(prev =>
      prev.includes(brand) ? prev.filter(b => b !== brand) : [...prev, brand]
    );
  };

  const selectCategory = (id) => {
    setSelectedCategory(prev => (prev === id ? null : id));
    setSidebarOpen(false);
  };

  const toggleExpand = (id) => {
    setExpandedCats(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const isExpanded = (id) => expandedCats[id] !== false;

  const clearFilters = () => {
    setSelectedCategory(null);
    setPriceRange({ min: globalMin, max: globalMax });
    setSelectedBrands([]);
  };

  const leftPct = globalMax > globalMin ? ((bounds.min - globalMin) / (globalMax - globalMin)) * 100 : 0;
  const widthPct = globalMax > globalMin ? ((bounds.max - bounds.min) / (globalMax - globalMin)) * 100 : 100;

  // ─── Sidebar content ───
  const sidebarContent = (
    <div className="space-y-8">
      {/* Categorías */}
      <div>
        <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-4">Comprar por categorías</h3>
        <ul className="space-y-1">
          <li>
            <button
              onClick={() => selectCategory(null)}
              className={`w-full flex items-center justify-between text-sm py-2 px-3 rounded-xl transition-colors ${selectedCategory === null ? 'bg-[#047857]/10 text-[#065F46] font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'}`}
            >
              <span>Todas las categorías</span>
              <span className="text-xs bg-slate-100 text-slate-500 px-2 py-0.5 rounded-full">{availableProducts.length}</span>
            </button>
          </li>
          {CATEGORIES.map(cat => {
            const catCount = cat.children
              ? cat.children.reduce((acc, sub) => acc + (categoryCounts[sub.id] || 0), 0)
              : (categoryCounts[cat.id] || 0);
            return (
              <li key={cat.id}>
                {cat.children ? (
                  <>
                    <button
                      onClick={() => toggleExpand(cat.id)}
                      className="w-full flex items-center justify-between text-sm py-2 px-3 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition-colors"
                    >
                      <span className="font-semibold text-slate-700">{cat.label}</span>
                      <span className="flex items-center gap-2">
                        <span className="text-xs bg-slate-100 text-slate-500 px-2 py-0.5 rounded-full">{catCount}</span>
                        <ChevronDown className={`w-4 h-4 text-slate-400 transition-transform duration-300 ${isExpanded(cat.id) ? 'rotate-180' : ''}`} />
                      </span>
                    </button>
                    {isExpanded(cat.id) && (
                      <ul className="ml-4 mt-1 space-y-1 border-l border-gray-100 pl-3">
                        {cat.children.map(sub => (
                          <li key={sub.id}>
                            <button
                              onClick={() => selectCategory(sub.id)}
                              className={`w-full flex items-center justify-between text-sm py-1.5 px-3 rounded-xl transition-colors ${selectedCategory === sub.id ? 'bg-[#047857]/10 text-[#065F46] font-semibold' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-50'}`}
                            >
                              <span>{sub.label}</span>
                              <span className="text-xs text-slate-400">{categoryCounts[sub.id] || 0}</span>
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </>
                ) : (
                  <button
                    onClick={() => selectCategory(cat.id)}
                    className={`w-full flex items-center justify-between text-sm py-2 px-3 rounded-xl transition-colors ${selectedCategory === cat.id ? 'bg-[#047857]/10 text-[#065F46] font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'}`}
                  >
                    <span>{cat.label}</span>
                    <span className="text-xs bg-slate-100 text-slate-500 px-2 py-0.5 rounded-full">{catCount}</span>
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      </div>

      {/* Precio */}
      <div className="border-t border-gray-100 pt-6">
        <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-4">Filtrar por precio</h3>
        <div className="relative h-6 mb-2">
          <div className="dual-track"></div>
          <div className="dual-range-fill" style={{ left: `${leftPct}%`, width: `${widthPct}%` }}></div>
          <input
            type="range"
            className="dual-range"
            min={globalMin}
            max={globalMax}
            step={5}
            value={bounds.min}
            onChange={e => setPriceRange(r => ({ ...r, min: Math.min(Number(e.target.value), (r ? r.max : globalMax) - 5) }))}
          />
          <input
            type="range"
            className="dual-range"
            min={globalMin}
            max={globalMax}
            step={5}
            value={bounds.max}
            onChange={e => setPriceRange(r => ({ ...r, max: Math.max(Number(e.target.value), (r ? r.min : globalMin) + 5) }))}
          />
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-[#065F46] font-bold">${bounds.min}</span>
          <span className="text-slate-300">–</span>
          <span className="text-[#065F46] font-bold">${bounds.max}</span>
        </div>
      </div>

      {/* Marcas */}
      <div className="border-t border-gray-100 pt-6">
        <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-4">Filtrar por marca</h3>
        {brandList.length === 0 ? (
          <p className="text-sm text-slate-400">No hay marcas disponibles.</p>
        ) : (
          <ul className="space-y-2">
            {brandList.map(brand => (
              <li key={brand}>
                <label className="flex items-center gap-3 cursor-pointer group">
                  <input
                    type="checkbox"
                    checked={selectedBrands.includes(brand)}
                    onChange={() => toggleBrand(brand)}
                    className="w-4 h-4 rounded accent-[#047857] border-gray-300"
                  />
                  <span className={`text-sm transition-colors ${selectedBrands.includes(brand) ? 'text-[#065F46] font-semibold' : 'text-slate-600 group-hover:text-slate-900'}`}>
                    {brand}
                  </span>
                  <span className="ml-auto text-xs text-slate-400">
                    {availableProducts.filter(p => getMarca(p) === brand).length}
                  </span>
                </label>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-[#06030F] font-sans text-slate-300 relative overflow-x-hidden selection:bg-cyan-500 selection:text-white">

      {/* Fondo del banner (slider) */}
      <div className="fixed inset-0 w-full h-full pointer-events-none z-0 overflow-hidden">
        {storeHeroSlides.map((slide, i) => (
          <img
            key={i}
            src={slide.image}
            alt="Veterinaria Sirbull Pet"
            className={`absolute inset-0 w-full h-full object-cover object-[50%_30%] lg:object-center transition-opacity duration-[1500ms] ${i === slideIndex ? 'opacity-100' : 'opacity-0'}`}
          />
        ))}
        <div className={`absolute inset-0 bg-black/40 transition-opacity duration-[1500ms] ${slideIndex === 1 ? 'opacity-100' : 'opacity-0'}`}></div>
        <div className="absolute inset-x-0 top-0 h-72 bg-gradient-to-b from-black/70 via-black/30 to-transparent"></div>
      </div>

      {/* Header unificado con carrito */}
      <SiteHeader
        variant="overlay"
        showContactBar
        showCart
        cartItemsCount={cartItemsCount}
        onCartOpen={() => setIsCartOpen(true)}
      />

      {/* Hero Section */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 sm:px-16 lg:px-24 pt-40 sm:pt-48 lg:pt-64 mb-16 lg:mb-32">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">

          <div key={slideIndex} className="lg:col-span-12 animate-fade-in-up pr-10 sm:pr-0">
            <p className="text-sm sm:text-base font-semibold tracking-wide text-white mb-4">
              {storeHeroSlides[slideIndex].tagline}
            </p>
            <h1 className="z-10 [font-family:Changa] text-left leading-tight lg:leading-[62px] font-bold text-4xl sm:text-5xl lg:text-[60px] w-full lg:w-[580px] mb-6 lg:mb-8 text-white">
              {storeHeroSlides[slideIndex].titleLine1} {storeHeroSlides[slideIndex].titleLine2} {storeHeroSlides[slideIndex].titleLine3}
            </h1>

            {(() => {
              const cta = storeHeroSlides[slideIndex].cta;
              const CtaIcon = cta.icon;
              const ContactIcon = cta.contact.icon;
              const isScroll = cta.to.startsWith('#');
              const isExternal = cta.to.startsWith('mailto:') || cta.to.startsWith('http');
              const btnClass = "inline-flex items-center px-7 py-3.5 rounded-full bg-[#047857] text-white hover:bg-[#065F46] shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all group";
              const btnContent = (
                <>
                  <div className="flex items-center">
                    <CtaIcon className="w-6 h-6 mr-3 text-emerald-100" />
                    <div className="text-left leading-tight mr-4">
                      <span className="block font-bold text-[14px] tracking-wide">{cta.label}</span>
                    </div>
                  </div>
                  <ChevronRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                </>
              );
              return (
                <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 sm:gap-5">
                  {isScroll ? (
                    <button onClick={() => document.getElementById('tienda')?.scrollIntoView({ behavior: 'smooth' })} className={btnClass}>{btnContent}</button>
                  ) : isExternal ? (
                    <a href={cta.to} className={btnClass}>{btnContent}</a>
                  ) : (
                    <Link to={cta.to} className={btnClass}>{btnContent}</Link>
                  )}
                  <div className="flex items-center gap-2">
                    <ContactIcon className="w-5 h-5 text-emerald-200 flex-shrink-0" />
                    <span className="text-sm sm:text-base font-semibold text-white">{cta.contact.text}</span>
                  </div>
                </div>
              );
            })()}

          </div>

        </div>
      </div>

      {/* Auth Alert */}
      {showAuthAlert && (
        <div className="fixed top-24 left-1/2 transform -translate-x-1/2 z-[210] animate-fade-in-up">
          <div className="bg-white text-slate-800 px-6 py-4 rounded-2xl shadow-2xl border border-red-200 flex items-center">
            <AlertCircle className="w-6 h-6 mr-3 text-red-500" />
            <div>
              <p className="font-bold">Inicia sesión para comprar</p>
              <p className="text-sm text-slate-500">Necesitas una cuenta para agregar productos al carrito.</p>
            </div>
            <button
              onClick={() => navigate('/login')}
              className="ml-6 bg-[#047857] text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-[#065F46] transition-colors"
            >
              Ir al Login
            </button>
            <button onClick={() => setShowAuthAlert(false)} className="ml-2 text-slate-400 hover:text-slate-700 p-2">✕</button>
          </div>
        </div>
      )}

      {/* Sección de la tienda */}
      <section id="tienda" className="relative z-20 bg-[#F9FAFB] shadow-[0_-10px_40px_rgba(0,0,0,0.35)]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 flex flex-col lg:flex-row gap-10 lg:gap-12">

          {/* Sidebar Desktop */}
          <aside className="hidden lg:block lg:w-72 flex-shrink-0">
            <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-6 lg:sticky lg:top-8">
              {sidebarContent}
              {(selectedCategory !== null || selectedBrands.length > 0 || (priceRange && (priceRange.min !== globalMin || priceRange.max !== globalMax))) && (
                <button
                  onClick={clearFilters}
                  className="mt-8 w-full text-sm font-bold text-[#047857] hover:text-[#065F46] border border-[#047857]/30 hover:border-[#047857]/50 rounded-xl py-2.5 transition-colors"
                >
                  Limpiar filtros
                </button>
              )}
            </div>
          </aside>

          {/* Mobile Sidebar Drawer */}
          {sidebarOpen && (
            <div className="fixed inset-0 z-[150] lg:hidden">
              <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setSidebarOpen(false)}></div>
              <div className="absolute left-0 top-0 h-full w-[85%] max-w-sm bg-white border-r border-gray-200 overflow-y-auto p-6 animate-slide-in-left">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-lg font-extrabold text-slate-900 uppercase tracking-widest flex items-center">
                    <SlidersHorizontal className="w-5 h-5 mr-3 text-[#047857]" /> Filtros
                  </h2>
                  <button onClick={() => setSidebarOpen(false)} className="text-slate-400 hover:text-slate-900 p-2">
                    <X className="w-6 h-6" />
                  </button>
                </div>
                {sidebarContent}
                <button
                  onClick={() => { clearFilters(); setSidebarOpen(false); }}
                  className="mt-8 w-full text-sm font-bold text-[#047857] hover:text-[#065F46] border border-[#047857]/30 hover:border-[#047857]/50 rounded-xl py-2.5 transition-colors"
                >
                  Limpiar filtros
                </button>
              </div>
            </div>
          )}

          {/* Main Store Content */}
          <div className="flex-1 transition-all duration-500 w-full">
            <div className="mb-8 animate-fade-in-up">
              <p className="text-sm font-bold text-[#047857] uppercase tracking-[0.2em] mb-2">Tienda Oficial</p>
              <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight text-slate-900 uppercase">
                Nuestros <span className="text-[#047857]">Productos</span>
              </h1>
              <p className="text-slate-500 mt-3 max-w-2xl leading-relaxed">
                Productos seleccionados por nuestros veterinarios para garantizar la salud integral de tu mascota.
              </p>
            </div>

            {loading ? (
              <div className="flex justify-center p-32">
                <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-[#047857]"></div>
              </div>
            ) : error ? (
              <div className="bg-red-50 border border-red-200 rounded-2xl p-8 text-center text-red-500">
                <AlertCircle className="w-12 h-12 mx-auto mb-4 opacity-50" />
                <p className="text-xl font-bold">{error}</p>
              </div>
            ) : (
              <>
                <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
                  <button
                    onClick={() => setSidebarOpen(true)}
                    className="lg:hidden flex items-center gap-2 text-sm font-bold text-slate-700 bg-white border border-gray-200 rounded-xl px-4 py-2.5 hover:border-[#047857]/50 hover:text-[#065F46] transition-colors"
                  >
                    <SlidersHorizontal className="w-4 h-4" /> Filtros
                  </button>
                  <p className="text-sm text-slate-500">
                    Mostrando <span className="text-[#065F46] font-bold">{filtered.length}</span> productos
                    {selectedCategory && (
                      <span className="ml-2 text-slate-400">· Categoría: <span className="text-[#065F46] font-semibold">{getCategoriaLabel(selectedCategory)}</span></span>
                    )}
                  </p>
                  {selectedCategory !== null || selectedBrands.length > 0 ? (
                    <button
                      onClick={clearFilters}
                      className="lg:hidden text-xs font-bold text-[#047857] hover:text-[#065F46]"
                    >
                      Limpiar filtros
                    </button>
                  ) : null}
                </div>

                {filtered.length === 0 ? (
                  <div className="bg-white border border-gray-200 rounded-2xl p-16 text-center">
                    <PackageSearch className="w-16 h-16 mx-auto mb-4 text-slate-300" />
                    <p className="text-xl font-bold text-slate-900 mb-2">Sin resultados</p>
                    <p className="text-slate-500 text-sm">Prueba ajustando los filtros o seleccionando otra categoría.</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-8">
                    {filtered.map((prod) => {
                      const rating = ratingFor(prod.id);
                      const reviews = reviewsFor(prod.id);
                      return (
                        <Card
                          key={prod.id}
                          variant="default"
                          className="group p-0 overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm hover:shadow-lg hover:shadow-gray-200/60 hover:-translate-y-1 transition-all duration-300"
                        >
                          <div className="relative overflow-hidden bg-gray-50 group/item cursor-pointer" onClick={() => setSelectedProduct(prod)}>
                            <div className="aspect-square flex items-center justify-center p-6">
                              <img
                                src={prod.imagen || getProductImage(prod.nombre)}
                                alt={prod.nombre}
                                className="max-h-full max-w-full object-contain z-20 group-hover/item:scale-110 transition-transform duration-500"
                              />

                              {prod.stock < 5 && (
                                <span className="absolute top-3 left-3 bg-red-500 text-white text-xs font-bold px-3 py-1 rounded-full z-30 shadow-sm">
                                  Últimos {prod.stock}
                                </span>
                              )}

                              {/* Overlay + Vista rápida */}
                              <div className="absolute inset-0 bg-slate-900/0 group-hover/item:bg-slate-900/30 transition-colors duration-300 z-10 flex items-center justify-center">
                                <span className="opacity-0 group-hover/item:opacity-100 translate-y-2 group-hover/item:translate-y-0 transition-all duration-300 bg-white text-slate-900 text-xs font-bold uppercase tracking-wider px-5 py-2.5 rounded-full shadow-lg">
                                  Vista rápida
                                </span>
                              </div>

                              {/* Acciones wishlist / comparar */}
                              <div className="absolute top-3 right-3 flex flex-col gap-2 z-30 opacity-0 group-hover/item:opacity-100 translate-x-3 group-hover/item:translate-x-0 transition-all duration-300">
                                <button
                                  onClick={e => { e.stopPropagation(); toast.info("Agregado a la lista de deseos (demo)."); }}
                                  title="Añadir a la lista de deseos"
                                  className="w-9 h-9 bg-white/95 backdrop-blur rounded-full flex items-center justify-center text-slate-500 hover:text-red-500 shadow-md"
                                >
                                  <Heart className="w-4 h-4" />
                                </button>
                                <button
                                  onClick={e => { e.stopPropagation(); toast.info("Agregado para comparar (demo)."); }}
                                  title="Agregar para comparar"
                                  className="w-9 h-9 bg-white/95 backdrop-blur rounded-full flex items-center justify-center text-slate-500 hover:text-[#065F46] shadow-md"
                                >
                                  <GitCompareArrows className="w-4 h-4" />
                                </button>
                              </div>
                            </div>
                          </div>

                          <div className="p-5 flex flex-col flex-grow bg-white relative z-20">
                            <p className="text-[11px] font-bold uppercase tracking-[0.15em] text-[#065F46] mb-2">{getCategoriaLabel(getCategoriaId(prod.nombre))}</p>
                            <h3
                              className="text-base font-bold text-slate-900 mb-2 leading-snug cursor-pointer hover:text-[#065F46] transition-colors line-clamp-2 min-h-[2.75rem]"
                              onClick={() => setSelectedProduct(prod)}
                            >
                              {prod.nombre}
                            </h3>

                            <div className="flex items-center gap-1.5 mb-4">
                              <Stars rating={rating} />
                              <span className="text-xs font-semibold text-slate-600">{rating.toFixed(2)}</span>
                              <span className="text-xs text-slate-400">({reviews})</span>
                            </div>

                            <div className="mt-auto pt-3 flex items-center justify-between border-t border-gray-100">
                              <span className="text-xl font-extrabold text-slate-900 leading-none">
                                <span className="text-xs align-top text-slate-400 mr-0.5">$</span>
                                {prod.precio.toFixed(2)}
                              </span>
                              <button
                                onClick={() => addToCart(prod)}
                                title="Añadir a la cesta"
                                className="w-11 h-11 rounded-full bg-[#047857] text-white flex items-center justify-center shadow-sm hover:bg-[#065F46] hover:shadow-lg hover:shadow-[#047857]/30 hover:-translate-y-0.5 active:translate-y-0 transition-all"
                              >
                                <ShoppingCart className="w-5 h-5" />
                              </button>
                            </div>
                          </div>
                        </Card>
                      );
                    })}
                  </div>
                )}
              </>
            )}
          </div>

        </div>
      </section>

      {/* Carrito: Drawer overlay (fuera de la segunda columna de la tienda) */}
      {isCartOpen && (
        <div className="fixed inset-0 z-[200]">
          <div className="absolute inset-0 bg-slate-900/50 backdrop-blur-sm animate-fade-in-backdrop" onClick={() => setIsCartOpen(false)}></div>

          <div className="absolute right-0 top-0 h-full w-full max-w-md bg-white shadow-2xl border-l border-gray-200 flex flex-col animate-slide-in-right">
            {/* Header */}
            <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-white">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-[#047857]/10 text-[#065F46] flex items-center justify-center">
                  <ShoppingCart className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-lg font-extrabold text-slate-900 leading-none">Tu Carrito</h2>
                  <p className="text-xs text-slate-400 mt-1">
                    {cartItemsCount > 0 ? `${cartItemsCount} producto${cartItemsCount !== 1 ? 's' : ''}` : '0 productos'}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsCartOpen(false)}
                aria-label="Cerrar carrito"
                className="w-10 h-10 rounded-full flex items-center justify-center text-slate-400 hover:text-slate-900 hover:bg-slate-100 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Items */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4">
              {cart.length === 0 ? (
                <div className="text-center text-slate-400 py-16 flex flex-col items-center h-full justify-center">
                  <div className="w-20 h-20 rounded-full bg-gray-50 border border-gray-100 flex items-center justify-center mb-5">
                    <PackageSearch className="w-10 h-10 text-slate-300" />
                  </div>
                  <p className="text-lg font-bold text-slate-900">Tu carrito está vacío</p>
                  <p className="text-sm mt-1">Agrega productos para comenzar tu pedido.</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.id} className="bg-gray-50 border border-gray-100 p-4 rounded-2xl flex items-center gap-4 group">
                    <div className="w-16 h-16 bg-white border border-gray-100 rounded-xl p-2 flex-shrink-0">
                      <img src={item.imagen || getProductImage(item.nombre)} alt={item.nombre} className="w-full h-full object-contain" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2">
                        <h4 className="text-sm font-bold text-slate-900 leading-snug">{item.nombre}</h4>
                        <button
                          onClick={() => removeFromCart(item.id)}
                          aria-label={`Eliminar ${item.nombre}`}
                          className="text-slate-300 hover:text-red-500 p-1.5 -mt-1.5 -mr-1.5 rounded-lg transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                      <p className="text-[#065F46] font-bold mt-1">
                        ${(item.precio * item.cantidad).toFixed(2)}
                        <span className="text-xs text-slate-400 font-normal ml-1">(${item.precio.toFixed(2)} c/u)</span>
                      </p>

                      <div className="flex items-center gap-3 mt-3">
                        <div className="flex items-center bg-white border border-gray-200 rounded-lg">
                          <button
                            onClick={() => updateQty(item.id, -1)}
                            disabled={item.cantidad <= 1}
                            aria-label="Disminuir cantidad"
                            className="p-1.5 text-slate-400 hover:text-slate-900 disabled:opacity-30 disabled:cursor-not-allowed"
                          >
                            <Minus className="w-3 h-3" />
                          </button>
                          <span className="text-xs font-bold w-6 text-center text-slate-900">{item.cantidad}</span>
                          <button
                            onClick={() => updateQty(item.id, 1)}
                            disabled={item.cantidad >= item.stock}
                            aria-label="Aumentar cantidad"
                            className="p-1.5 text-slate-400 hover:text-slate-900 disabled:opacity-30 disabled:cursor-not-allowed"
                          >
                            <Plus className="w-3 h-3" />
                          </button>
                        </div>
                        {item.cantidad >= item.stock && (
                          <span className="text-[10px] font-bold text-red-500 bg-red-50 px-2 py-0.5 rounded-full">
                            Stock máximo
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Footer */}
            {cart.length > 0 && (
              <div className="p-6 border-t border-gray-100 bg-white">
                <div className="space-y-2 mb-6">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Subtotal</span>
                    <span className="text-slate-900 font-bold">${cartTotal.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Envío</span>
                    <span className="text-emerald-600 font-bold">Gratis</span>
                  </div>
                  <div className="flex justify-between items-center pt-3 border-t border-gray-100">
                    <span className="text-slate-900 font-bold uppercase tracking-widest text-sm">Total</span>
                    <span className="text-3xl font-black text-slate-900">${cartTotal.toFixed(2)}</span>
                  </div>
                </div>

                {checkoutStatus === 'success' ? (
                  <div className="bg-emerald-50 border border-emerald-200 p-4 rounded-xl text-center">
                    <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto mb-2" />
                    <p className="font-bold text-emerald-600">¡Pedido Confirmado!</p>
                    <p className="text-xs text-emerald-700/70 mt-1">Recibirás un correo con los detalles.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {checkoutError && <p className="text-red-500 text-sm font-bold text-center bg-red-50 py-2 rounded-lg">{checkoutError}</p>}

                    <Button
                      onClick={openCheckout}
                      variant="primary"
                      className="w-full py-4 text-base bg-[#047857] hover:bg-[#065F46] shadow-sm hover:shadow-md hover:shadow-[#047857]/30"
                    >
                      <CreditCard className="w-5 h-5 mr-2" /> Finalizar Compra
                    </Button>

                    <div className="flex items-center justify-center text-xs text-slate-400">
                      <Shield className="w-3 h-3 mr-1" /> Transacción Segura
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Modal de Checkout */}
      {showCheckout && (
        <div className="fixed inset-0 z-[210]">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm animate-fade-in-backdrop" onClick={() => { if (checkoutStatus !== 'loading') { setShowCheckout(false); setVentaConfirmada(null); } }}></div>

          <div className="absolute right-0 top-0 h-full w-full max-w-md bg-white shadow-2xl border-l border-gray-200 flex flex-col animate-slide-in-right">
            {checkoutStatus === 'success' && ventaConfirmada ? (
              <>
                <div className="flex-1 overflow-y-auto p-6">
                  <div className="text-center pt-8">
                    <div className="w-20 h-20 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto mb-5">
                      <CheckCircle2 className="w-10 h-10" />
                    </div>
                    <h2 className="text-2xl font-extrabold text-slate-900">¡Compra registrada!</h2>
                    <p className="text-slate-500 mt-2 text-sm">
                      Tu pedido quedó <span className="font-bold text-emerald-600">Pendiente</span>. Te avisaremos cuando esté confirmado para que lo recojas en la veterinaria.
                    </p>
                  </div>

                  <div className="mt-8 bg-emerald-50 border border-emerald-200 rounded-2xl p-5 text-center">
                    <p className="text-xs font-bold uppercase tracking-widest text-emerald-700/70 mb-1">Tu código de recojo</p>
                    <p className="text-3xl font-black text-emerald-700 tracking-wider" style={{ fontFamily: 'monospace' }}>{ventaConfirmada.codigo_recojo}</p>
                    <p className="text-xs text-emerald-700/70 mt-2">Preséntalo en caja al recoger tu pedido.</p>
                  </div>

                  <div className="mt-6 rounded-2xl border-2 border-dashed border-slate-200 p-5">
                    <div className="text-center pb-3 border-b border-dashed border-slate-200">
                      <p className="font-extrabold text-slate-900 tracking-wide">VETERINARIA SIRBULL</p>
                      <p className="text-xs text-slate-500 mt-1">Av. Los Gladiolos 123, San Juan de Lurigancho</p>
                      <p className="text-xs text-slate-500">RUC: 20513245442</p>
                    </div>
                    <div className="space-y-3 pt-3">
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Comprobante</span>
                      <span className="text-slate-900 font-bold">{ventaConfirmada.tipo_comprobante}</span>
                    </div>
                    {ventaConfirmada.documento_fiscal && (
                      <div className="flex justify-between text-sm">
                        <span className="text-slate-500">{tipoDocLabel(ventaConfirmada.documento_fiscal, ventaConfirmada.tipo_comprobante)}</span>
                        <span className="text-slate-900 font-bold">{ventaConfirmada.documento_fiscal}</span>
                      </div>
                    )}
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Método de pago</span>
                      <span className="text-slate-900 font-bold">{ventaConfirmada.metodo_pago}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-slate-500">Estado</span>
                      <span className="text-amber-600 font-bold">{ventaConfirmada.estado}</span>
                    </div>
                    <div className="flex justify-between items-center pt-3 border-t border-gray-100">
                      <span className="text-slate-900 font-bold uppercase tracking-widest text-sm">Total</span>
                      <span className="text-2xl font-black text-slate-900">${ventaConfirmada.total.toFixed(2)}</span>
                    </div>
                    </div>
                  </div>
                </div>

                <div className="p-6 border-t border-gray-100 space-y-3">
                  <Button
                    onClick={() => navigate('/portal')}
                    variant="primary"
                    className="w-full py-4 text-base bg-[#047857] hover:bg-[#065F46]"
                  >
                    Ver mis compras
                  </Button>
                  <Button
                    onClick={() => { setShowCheckout(false); setVentaConfirmada(null); }}
                    variant="outline"
                    className="w-full py-3.5"
                  >
                    Seguir comprando
                  </Button>
                </div>
              </>
            ) : (
              <>
                <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-[#047857]/10 text-[#065F46] flex items-center justify-center">
                      <CreditCard className="w-5 h-5" />
                    </div>
                    <div>
                      <h2 className="text-lg font-extrabold text-slate-900 leading-none">Finalizar Compra</h2>
                      <p className="text-xs text-slate-400 mt-1">Completa los datos de tu pedido</p>
                    </div>
                  </div>
                  <button
                    onClick={() => setShowCheckout(false)}
                    disabled={checkoutStatus === 'loading'}
                    aria-label="Cerrar checkout"
                    className="w-10 h-10 rounded-full flex items-center justify-center text-slate-400 hover:text-slate-900 hover:bg-slate-100 transition-colors disabled:opacity-30"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                  {/* Resumen del pedido */}
                  <div>
                    <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3">Resumen del pedido</h3>
                    <div className="bg-gray-50 border border-gray-100 rounded-2xl p-4 space-y-2">
                      {cart.map(item => (
                        <div key={item.id} className="flex items-center justify-between text-sm gap-3">
                          <span className="text-slate-700 truncate flex-1">
                            {item.cantidad} × {item.nombre}
                          </span>
                          <span className="text-slate-900 font-bold whitespace-nowrap">${(item.precio * item.cantidad).toFixed(2)}</span>
                        </div>
                      ))}
                      <div className="flex justify-between items-center pt-3 border-t border-gray-100">
                        <span className="text-slate-900 font-bold uppercase tracking-widest text-xs">Total</span>
                        <span className="text-xl font-black text-slate-900">${cartTotal.toFixed(2)}</span>
                      </div>
                    </div>
                  </div>

                  {/* Tipo de comprobante */}
                  <div>
                    <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3">Tipo de comprobante</h3>
                    <div className="grid grid-cols-2 gap-3">
                      {['Boleta', 'Factura'].map(t => (
                        <button
                          key={t}
                          onClick={() => {
                            setTipoComprobante(t)
                            setTipoDocumento(t === 'Factura' ? 'RUC' : 'DNI')
                            setDocumentoFiscal('')
                          }}
                          className={`border rounded-xl px-4 py-3 text-sm font-bold transition-colors ${tipoComprobante === t ? 'border-[#047857] bg-[#047857]/10 text-[#065F46]' : 'border-gray-200 text-slate-600 hover:border-gray-300'}`}
                        >
                          {t}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Documento del comprobante */}
                  <div>
                    <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3">Tipo de documento</h3>
                    <div className="grid grid-cols-3 gap-3">
                      {(tipoComprobante === 'Factura' ? ['RUC'] : ['DNI', 'CE']).map(td => (
                        <button
                          key={td}
                          onClick={() => setTipoDocumento(td)}
                          className={`border rounded-xl px-3 py-3 text-sm font-bold transition-colors ${tipoDocumento === td ? 'border-[#047857] bg-[#047857]/10 text-[#065F46]' : 'border-gray-200 text-slate-600 hover:border-gray-300'}`}
                        >
                          {td} <span className="block text-[10px] font-normal">{maxDigitos[td]} díg.</span>
                        </button>
                      ))}
                    </div>
                    <div className="mt-3">
                      <input
                        type="text"
                        inputMode="numeric"
                        maxLength={maxDigitos[tipoDocumento]}
                        value={documentoFiscal}
                        onChange={e => setDocumentoFiscal(e.target.value.replace(/\D/g, ''))}
                        placeholder={`Ingresa tu ${tipoDocumento} (${maxDigitos[tipoDocumento]} dígitos)`}
                        className="w-full border border-gray-200 rounded-xl px-4 py-3 text-sm font-semibold text-slate-900 placeholder:text-slate-400 placeholder:font-normal focus:border-[#047857] focus:ring-2 focus:ring-[#047857]/20 outline-none transition"
                      />
                    </div>
                  </div>

                  {/* Método de pago */}
                  <div>
                    <h3 className="text-xs font-extrabold text-slate-900 uppercase tracking-wider mb-3">Método de pago</h3>
                    <div className="space-y-2">
                      {[
                        { id: 'Efectivo', icon: Banknote },
                        { id: 'Tarjeta', icon: CreditCard },
                        { id: 'Yape', icon: Smartphone },
                      ].map(m => {
                        const Icon = m.icon;
                        return (
                          <button
                            key={m.id}
                            onClick={() => setMetodoPago(m.id)}
                            className={`w-full flex items-center gap-3 border rounded-xl px-4 py-3 text-sm font-bold transition-colors ${metodoPago === m.id ? 'border-[#047857] bg-[#047857]/10 text-[#065F46]' : 'border-gray-200 text-slate-600 hover:border-gray-300'}`}
                          >
                            <Icon className="w-5 h-5" />
                            {m.id}
                            {m.id === 'Yape' && <span className="text-xs font-normal text-slate-400 ml-auto">Pago al recojo</span>}
                            {m.id === 'Tarjeta' && <span className="text-xs font-normal text-slate-400 ml-auto">Pago al recojo</span>}
                          </button>
                        );
                      })}
                    </div>
                  </div>

                  <div className="bg-sky-50 border border-sky-200 rounded-xl p-4 flex items-start gap-3">
                    <Store className="w-5 h-5 text-sky-600 flex-shrink-0 mt-0.5" />
                    <p className="text-xs text-sky-800 leading-relaxed">
                      <span className="font-bold">Recojo en tienda.</span> Generaremos un código con el que recogerás tu pedido en la veterinaria una vez confirmado.
                    </p>
                  </div>
                </div>

                <div className="p-6 border-t border-gray-100 space-y-3">
                  {checkoutError && (
                    <p className="text-red-500 text-sm font-bold text-center bg-red-50 py-2 rounded-lg">{checkoutError}</p>
                  )}
                  <Button
                    onClick={handleCheckout}
                    variant="primary"
                    className="w-full py-4 text-base bg-[#047857] hover:bg-[#065F46] shadow-sm hover:shadow-md hover:shadow-[#047857]/30"
                    isLoading={checkoutStatus === 'loading'}
                  >
                    <Lock className="w-4 h-4 mr-2" /> Confirmar pedido
                  </Button>
                  <div className="flex items-center justify-center text-xs text-slate-400">
                    <Shield className="w-3 h-3 mr-1" /> Transacción Segura
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
