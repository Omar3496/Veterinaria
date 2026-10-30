// Mapeo dinámico de imágenes para productos basado en su nombre
export const getProductImage = (nombre) => {
  const nombreMin = nombre.toLowerCase();
  
  if (nombreMin.includes('croqueta') || nombreMin.includes('alimento') || nombreMin.includes('comida') || nombreMin.includes('pienso')) {
    return 'https://images.unsplash.com/photo-1589924691995-400dc9ce5ce1?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Perro comiendo / Alimento
  }
  if (nombreMin.includes('juguete') || nombreMin.includes('pelota') || nombreMin.includes('hueso') || nombreMin.includes('cuerda')) {
    return 'https://images.unsplash.com/photo-1576201836106-db1758fd1c97?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Juguetes perro
  }
  if (nombreMin.includes('correa') || nombreMin.includes('collar') || nombreMin.includes('arnés') || nombreMin.includes('arnes')) {
    return 'https://images.unsplash.com/photo-1601635567303-10e82c815ec6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Perro con correa
  }
  if (nombreMin.includes('cama') || nombreMin.includes('manta') || nombreMin.includes('colchón')) {
    return 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Perro durmiendo en cama
  }
  if (nombreMin.includes('champú') || nombreMin.includes('champu') || nombreMin.includes('cepillo') || nombreMin.includes('jabón') || nombreMin.includes('higiene')) {
    return 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Perro mojado / baño
  }
  if (nombreMin.includes('vacuna') || nombreMin.includes('jeringa') || nombreMin.includes('inyección')) {
    return 'https://images.unsplash.com/photo-1628102491629-778571d893a3?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Insumos médicos
  }
  if (nombreMin.includes('pastilla') || nombreMin.includes('medicamento') || nombreMin.includes('antibiótico') || nombreMin.includes('desparasitante')) {
    return 'https://images.unsplash.com/photo-1585435557343-3b092031a831?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'; // Medicamentos
  }
  
  // Imagen por defecto genérica (accesorios/clínica)
  return 'https://images.unsplash.com/photo-1596492784531-6e6eb5ea9993?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80';
};
