from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, DateTime, Date, Float, Enum, Text
from sqlalchemy.orm import relationship
import enum
import datetime
from database import Base

class RolEnum(enum.Enum):
    ADMIN = "Administrador"
    VETERINARIO = "Veterinario"
    RECEPCIONISTA = "Recepcionista"
    CLIENTE = "Cliente"

class Usuario(Base):
    __tablename__ = "usuarios"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)
    rol = Column(Enum(RolEnum), default=RolEnum.RECEPCIONISTA)
    is_active = Column(Boolean, default=True)
    email = Column(String, nullable=True)

class Cliente(Base):
    __tablename__ = "clientes"
    id = Column(Integer, primary_key=True, index=True)
    dni = Column(String, unique=True, index=True)
    nombre = Column(String)
    telefono = Column(String, unique=True)
    email = Column(String, nullable=True)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    foto_url = Column(String, nullable=True)
    
    usuario = relationship("Usuario")
    mascotas = relationship("Mascota", back_populates="propietario", cascade="all, delete-orphan")

class Mascota(Base):
    __tablename__ = "mascotas"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, index=True)
    especie = Column(String)
    raza = Column(String, nullable=True)
    fecha_nacimiento = Column(Date, nullable=True)
    foto_url = Column(String, nullable=True)
    activo = Column(Boolean, default=True)
    cliente_id = Column(Integer, ForeignKey("clientes.id"))
    
    propietario = relationship("Cliente", back_populates="mascotas")
    citas = relationship("Cita", back_populates="mascota", cascade="all, delete-orphan")
    consultas = relationship("Consulta", back_populates="mascota")
    vacunas = relationship("Vacuna", back_populates="mascota")

class EstadoCita(enum.Enum):
    PENDIENTE = "Pendiente"
    PROGRAMADA = "Programada"
    COMPLETADA = "Completada"
    CANCELADA = "Cancelada"

class Cita(Base):
    __tablename__ = "citas"
    id = Column(Integer, primary_key=True, index=True)
    fecha_hora = Column(DateTime)
    motivo = Column(String)
    tipo = Column(String, default="Consulta médica")
    examen_auxiliar_id = Column(Integer, ForeignKey("examenes_auxiliares.id"), nullable=True)
    estado = Column(Enum(EstadoCita), default=EstadoCita.PROGRAMADA)
    motivo_cancelacion = Column(String, nullable=True)
    mascota_id = Column(Integer, ForeignKey("mascotas.id"))
    veterinario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    
    mascota = relationship("Mascota", back_populates="citas")
    veterinario = relationship("Usuario")
    examen_auxiliar = relationship("ExamenAuxiliar")
    orden_examen = relationship("OrdenExamen", back_populates="cita", uselist=False)

    @property
    def veterinario_nombre(self):
        return self.veterinario.username if self.veterinario else None

    @property
    def examen_auxiliar_nombre(self):
        return self.examen_auxiliar.nombre if self.examen_auxiliar else None

class Consulta(Base):
    __tablename__ = "consultas"
    id = Column(Integer, primary_key=True, index=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    diagnostico = Column(String)
    tratamiento = Column(String)
    peso = Column(Float, nullable=True)
    veterinario_id = Column(Integer, ForeignKey("usuarios.id"))
    mascota_id = Column(Integer, ForeignKey("mascotas.id"))
    cita_id = Column(Integer, ForeignKey("citas.id"), nullable=True)
    
    mascota = relationship("Mascota", back_populates="consultas")
    cita = relationship("Cita")
    recetas = relationship("RecetaMedica", back_populates="consulta", cascade="all, delete-orphan")
    examenes = relationship("OrdenExamen", back_populates="consulta")
    referencias = relationship("ReferenciaMedica", back_populates="consulta", cascade="all, delete-orphan")

    @property
    def cita_motivo(self):
        return self.cita.motivo if self.cita else None

class ExamenAuxiliar(Base):
    __tablename__ = "examenes_auxiliares"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, unique=True, index=True)
    descripcion = Column(Text, nullable=True)
    precio = Column(Float, default=0.0)
    tiempo_estimado = Column(String, nullable=True)
    activo = Column(Boolean, default=True)

    def __repr__(self):
        return f"<ExamenAuxiliar {self.nombre}>"

class RecetaMedica(Base):
    __tablename__ = "recetas"
    id = Column(Integer, primary_key=True, index=True)
    consulta_id = Column(Integer, ForeignKey("consultas.id"))
    producto_id = Column(Integer, ForeignKey("productos.id"), nullable=True)
    nombre_medicamento = Column(String)
    dosis = Column(String, nullable=True)
    cantidad = Column(String, nullable=True)
    indicaciones = Column(Text, nullable=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)

    consulta = relationship("Consulta", back_populates="recetas")
    producto = relationship("Producto")

    @property
    def producto_nombre(self):
        return self.producto.nombre if self.producto else None

class OrdenExamen(Base):
    __tablename__ = "ordenes_examen"
    id = Column(Integer, primary_key=True, index=True)
    consulta_id = Column(Integer, ForeignKey("consultas.id"), nullable=True)
    cita_id = Column(Integer, ForeignKey("citas.id"), nullable=True)
    examen_auxiliar_id = Column(Integer, ForeignKey("examenes_auxiliares.id"))
    resultado = Column(Text, nullable=True)
    estado = Column(String, default="PENDIENTE")
    observaciones = Column(Text, nullable=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)

    consulta = relationship("Consulta", back_populates="examenes")
    cita = relationship("Cita", back_populates="orden_examen")
    examen_auxiliar = relationship("ExamenAuxiliar")

    @property
    def examen_nombre(self):
        return self.examen_auxiliar.nombre if self.examen_auxiliar else None

class ReferenciaMedica(Base):
    __tablename__ = "referencias"
    id = Column(Integer, primary_key=True, index=True)
    consulta_id = Column(Integer, ForeignKey("consultas.id"))
    centro_destino = Column(String)
    especialidad = Column(String, nullable=True)
    motivo = Column(Text)
    estado = Column(String, default="ACTIVA")
    fecha = Column(DateTime, default=datetime.datetime.utcnow)

    consulta = relationship("Consulta", back_populates="referencias")

class Vacuna(Base):
    __tablename__ = "vacunas"
    id = Column(Integer, primary_key=True, index=True)
    tipo = Column(String)
    fecha_aplicacion = Column(Date)
    proxima_dosis = Column(Date, nullable=True)
    producto_id = Column(Integer, ForeignKey("productos.id"), nullable=True)
    intervalo_dias = Column(Integer, nullable=True)
    mascota_id = Column(Integer, ForeignKey("mascotas.id"))
    veterinario_id = Column(Integer, ForeignKey("usuarios.id"))
    
    mascota = relationship("Mascota", back_populates="vacunas")
    producto = relationship("Producto")

    @property
    def producto_nombre(self):
        return self.producto.nombre if self.producto else None

class Proveedor(Base):
    __tablename__ = "proveedores"
    id = Column(Integer, primary_key=True, index=True)
    ruc = Column(String, unique=True, index=True)
    razon_social = Column(String)
    contacto = Column(String, nullable=True)
    telefono = Column(String, nullable=True)
    direccion = Column(String, nullable=True)
    email = Column(String, nullable=True)
    moneda = Column(String, default="SOL")
    
    productos = relationship("Producto", back_populates="proveedor")
    ingresos = relationship("Ingreso", back_populates="proveedor")

class EstadoAlmacen(enum.Enum):
    DISPONIBLE = "Disponible"
    NO_DISPONIBLE = "No disponible"
    DANIADO = "Dañado"
    VENCIDO = "Vencido"

class Categoria(Base):
    __tablename__ = "categorias"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, unique=True, index=True)

    productos = relationship("Producto", back_populates="categoria")

class Familia(Base):
    __tablename__ = "familias"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, unique=True, index=True)

    productos = relationship("Producto", back_populates="familia")

class Marca(Base):
    __tablename__ = "marcas"
    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String, unique=True, index=True)

    productos = relationship("Producto", back_populates="marca")

class Producto(Base):
    __tablename__ = "productos"
    id = Column(Integer, primary_key=True, index=True)
    sku = Column(String, unique=True, index=True)
    codigo_barra = Column(String, nullable=True)
    imagen = Column(Text, nullable=True)
    nombre = Column(String)
    descripcion = Column(String, nullable=True)
    precio = Column(Float)
    stock = Column(Integer, default=0)
    stock_minimo = Column(Integer, default=5)
    fecha_vencimiento = Column(Date, nullable=True)
    es_perecible = Column(Boolean, default=False)
    precio_compra = Column(Float, default=0.0)
    estado_almacen = Column(Enum(EstadoAlmacen), default=EstadoAlmacen.DISPONIBLE)
    proveedor_id = Column(Integer, ForeignKey("proveedores.id"), nullable=True)
    categoria_id = Column(Integer, ForeignKey("categorias.id"), nullable=True)
    familia_id = Column(Integer, ForeignKey("familias.id"), nullable=True)
    marca_id = Column(Integer, ForeignKey("marcas.id"), nullable=True)

    proveedor = relationship("Proveedor", back_populates="productos")
    categoria = relationship("Categoria", back_populates="productos")
    familia = relationship("Familia", back_populates="productos")
    marca = relationship("Marca", back_populates="productos")
    movimientos = relationship("MovimientoStock", back_populates="producto", order_by="desc(MovimientoStock.fecha)")
    stocks_almacen = relationship("StockAlmacen", back_populates="producto", cascade="all, delete-orphan")

    @property
    def proveedor_nombre(self):
        return self.proveedor.razon_social if self.proveedor else None

    @property
    def categoria_nombre(self):
        return self.categoria.nombre if self.categoria else None

    @property
    def familia_nombre(self):
        return self.familia.nombre if self.familia else None

    @property
    def marca_nombre(self):
        return self.marca.nombre if self.marca else None

    @property
    def estado_almacen_nombre(self):
        return self.estado_almacen.value if self.estado_almacen else None

    @property
    def stock_por_almacen(self):
        return [{"almacen": s.almacen, "cantidad": s.cantidad} for s in self.stocks_almacen if (s.cantidad or 0) > 0]

class StockAlmacen(Base):
    __tablename__ = "productos_stock_almacen"
    id = Column(Integer, primary_key=True, index=True)
    producto_id = Column(Integer, ForeignKey("productos.id"), index=True)
    almacen = Column(String, default="Disponible")
    cantidad = Column(Integer, default=0)

    producto = relationship("Producto", back_populates="stocks_almacen")

class MovimientoStock(Base):
    __tablename__ = "movimientos_stock"
    id = Column(Integer, primary_key=True, index=True)
    producto_id = Column(Integer, ForeignKey("productos.id"))
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    tipo_movimiento = Column(String)
    cantidad = Column(Integer, default=0)
    estado_anterior = Column(String, nullable=True)
    estado_nuevo = Column(String, nullable=True)
    observacion = Column(String, nullable=True)
    referencia = Column(String, nullable=True)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)

    producto = relationship("Producto", back_populates="movimientos")
    usuario = relationship("Usuario")

    @property
    def producto_nombre(self):
        return self.producto.nombre if self.producto else None

    @property
    def usuario_nombre(self):
        return self.usuario.username if self.usuario else None

class Ingreso(Base):
    __tablename__ = "ingresos"
    id = Column(Integer, primary_key=True, index=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    proveedor_id = Column(Integer, ForeignKey("proveedores.id"))
    tipo_pago = Column(String)
    numero_factura = Column(String, nullable=True)
    tipo_comprobante = Column(String, default="Factura")
    serie = Column(String, nullable=True)
    numero = Column(String, nullable=True)
    total = Column(Float, default=0.0)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"))
    
    proveedor = relationship("Proveedor", back_populates="ingresos")
    detalles = relationship("IngresoDetalle", back_populates="ingreso", cascade="all, delete-orphan")

    @property
    def proveedor_nombre(self):
        return self.proveedor.razon_social if self.proveedor else None

class IngresoDetalle(Base):
    __tablename__ = "ingresos_detalle"
    id = Column(Integer, primary_key=True, index=True)
    ingreso_id = Column(Integer, ForeignKey("ingresos.id"))
    producto_id = Column(Integer, ForeignKey("productos.id"))
    cantidad = Column(Integer)
    precio_unitario = Column(Float)
    precio_venta = Column(Float, nullable=True)
    fecha_vencimiento = Column(Date, nullable=True)
    subtotal = Column(Float)
    
    ingreso = relationship("Ingreso", back_populates="detalles")
    producto = relationship("Producto")

    @property
    def producto_nombre(self):
        return self.producto.nombre if self.producto else None

class VentaEstado(enum.Enum):
    PENDIENTE = "Pendiente"
    CONFIRMADA = "Confirmada"
    ENTREGADA = "Entregada"
    CANCELADA = "Cancelada"
    ANULACION_SOLICITADA = "Anulación solicitada"

class Venta(Base):
    __tablename__ = "ventas"
    id = Column(Integer, primary_key=True, index=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    cliente_id = Column(Integer, ForeignKey("clientes.id"))
    usuario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    total = Column(Float, default=0.0)
    metodo_pago = Column(String)
    tipo_comprobante = Column(String, default="Boleta")
    documento_fiscal = Column(String, nullable=True)
    serie = Column(String, nullable=True)
    numero = Column(String, nullable=True)
    codigo_recojo = Column(String, nullable=True, index=True)
    estado = Column(Enum(VentaEstado), default=VentaEstado.PENDIENTE)
    nota_credito_id = Column(Integer, nullable=True)
    monto_nota = Column(Float, default=0.0)
    
    cliente = relationship("Cliente")
    usuario = relationship("Usuario")
    detalles = relationship("DetalleVenta", back_populates="venta", cascade="all, delete-orphan")

    @property
    def cliente_nombre(self):
        return self.cliente.nombre if self.cliente else None

    @property
    def vendedor_nombre(self):
        return self.usuario.username if self.usuario else None

    @property
    def estado_nombre(self):
        return self.estado.value if self.estado else None

    @property
    def comprobante(self):
        if self.serie and self.numero:
            return f"{self.serie}-{self.numero}"
        return self.documento_fiscal

class NotaCredito(Base):
    __tablename__ = "notas_credito"
    id = Column(Integer, primary_key=True, index=True)
    codigo = Column(String, unique=True, index=True)
    cliente_id = Column(Integer, ForeignKey("clientes.id"))
    venta_id = Column(Integer, nullable=True)
    monto = Column(Float, default=0.0)
    saldo = Column(Float, default=0.0)
    fecha_emision = Column(DateTime, default=datetime.datetime.utcnow)
    fecha_vencimiento = Column(DateTime, nullable=True)
    estado = Column(String, default="ACTIVA")
    motivo = Column(String, nullable=True)

    cliente = relationship("Cliente")

    @property
    def cliente_nombre(self):
        return self.cliente.nombre if self.cliente else None

    @property
    def vigente(self):
        if self.estado != "ACTIVA":
            return False
        if self.fecha_vencimiento and self.fecha_vencimiento < datetime.datetime.utcnow():
            return False
        return (self.saldo or 0) > 0

class Devolucion(Base):
    __tablename__ = "devoluciones"
    id = Column(Integer, primary_key=True, index=True)
    venta_id = Column(Integer, ForeignKey("ventas.id"))
    cliente_id = Column(Integer, ForeignKey("clientes.id"), nullable=True)
    tipo = Column(String)
    monto = Column(Float, default=0.0)
    motivo = Column(String, nullable=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    nota_credito_id = Column(Integer, nullable=True)

    venta = relationship("Venta")
    usuario = relationship("Usuario")

class ComprobanteSeq(Base):
    __tablename__ = "comprobantes_seq"
    id = Column(Integer, primary_key=True, index=True)
    serie = Column(String, unique=True, index=True)
    ultimo_numero = Column(Integer, default=0)

class DetalleVenta(Base):
    __tablename__ = "detalles_venta"
    id = Column(Integer, primary_key=True, index=True)
    venta_id = Column(Integer, ForeignKey("ventas.id"))
    producto_id = Column(Integer, ForeignKey("productos.id"))
    cantidad = Column(Integer)
    precio_unitario = Column(Float)
    subtotal = Column(Float)
    
    venta = relationship("Venta", back_populates="detalles")
    producto = relationship("Producto")

    @property
    def producto_nombre(self):
        return self.producto.nombre if self.producto else None

class Auditoria(Base):
    __tablename__ = "auditorias"
    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"), nullable=True)
    accion = Column(String)
    endpoint = Column(String)
    resource_id = Column(String, nullable=True)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    detalle = Column(String, nullable=True)
    
    usuario = relationship("Usuario")

class Configuracion(Base):
    __tablename__ = "configuracion"
    clave = Column(String, primary_key=True, index=True)
    valor = Column(String, nullable=True)


class Pago(Base):
    __tablename__ = "pagos"
    id = Column(Integer, primary_key=True, index=True)
    cita_id = Column(Integer, ForeignKey("citas.id"), nullable=True)
    monto = Column(Float, default=0.0)
    metodo = Column(String)  # e.g., YAPE, PLIN, TARJETA, EFECTIVO
    estado = Column(String, default="PENDIENTE")  # PENDIENTE, COMPLETADO, FALLIDO
    referencia = Column(String, nullable=True)
    detalle = Column(String, nullable=True)
    fecha = Column(DateTime, default=datetime.datetime.utcnow)
    tipo_comprobante = Column(String, default="Boleta")  # Boleta, Factura
    documento_fiscal = Column(String, nullable=True)  # DNI o RUC
    serie = Column(String, nullable=True)
    numero = Column(String, nullable=True)

    cita = relationship("Cita")

    @property
    def comprobante(self):
        if self.serie and self.numero:
            return f"{self.serie}-{self.numero}"
        return self.documento_fiscal

class PasswordResetToken(Base):
    __tablename__ = "password_reset_tokens"
    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey("usuarios.id"))
    token_hash = Column(String, unique=True, index=True)
    expires_at = Column(DateTime)
    used = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    usuario = relationship("Usuario")
