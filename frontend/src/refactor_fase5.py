import os
import re

directory = r"C:\Users\User\Desktop\D1_II\Veterinaria\frontend\src\pages"

files_to_process = [
    "Citas.jsx", "Inventario.jsx", "Clientes.jsx", "Ventas.jsx",
    "Historial.jsx", "Reportes.jsx", "Auditoria.jsx", "PortalCliente.jsx", "StoreList.jsx"
]

for filename in files_to_process:
    filepath = os.path.join(directory, filename)
    if not os.path.exists(filepath):
        continue
        
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # 1. Add imports
    if "useToast" not in content:
        content = re.sub(
            r"(import React.*?from 'react';?)", 
            r"\1\nimport { useToast } from '../components/ui/ToastProvider';\nimport { TableSkeleton } from '../components/ui/Skeleton';", 
            content
        )
        if "useToast" not in content: # fallback if the above didn't match
            content = content.replace(
                "import { useState, useEffect } from 'react'",
                "import { useState, useEffect } from 'react'\nimport { useToast } from '../components/ui/ToastProvider'\nimport { TableSkeleton } from '../components/ui/Skeleton'"
            )

    # 2. Add const toast = useToast()
    if "const toast = useToast()" not in content:
        content = re.sub(
            r"(export default function \w+\(\) {\n)",
            r"\1  const toast = useToast();\n",
            content
        )

    # 3. Remove error states
    content = re.sub(r"const \[error, setError\] = useState\(.*?\)\n?", "", content)
    content = re.sub(r"const \[success, setSuccess\] = useState\(.*?\)\n?", "", content)
    content = re.sub(r"const \[ventaExitosa, setVentaExitosa\] = useState\(.*?\)\n?", "", content)

    # 4. Replace setError and setSuccess
    content = re.sub(r'setError\((.*?)\)', r'toast.error(\1)', content)
    content = re.sub(r'setSuccess\((.*?)\)', r'toast.success(\1)', content)
    content = re.sub(r'setVentaExitosa\((.*?)\)', r'toast.success(\1)', content)
    
    # Remove setTimeout for error/success
    content = re.sub(r'setTimeout\(\(\) => (setError|setSuccess|setVentaExitosa)\(null\), \d+\)\n?', '', content)

    # 5. Remove error UI block
    content = re.sub(r'\{error && \(\s*<div.*?>.*?\{error\}.*?</div>\s*\)\}\n?', '', content, flags=re.DOTALL)
    content = re.sub(r'\{error && <div.*?>\{error\}</div>\}\n?', '', content, flags=re.DOTALL)
    content = re.sub(r'\{error && <div.*?>.*?\{error\}.*?</div>\}\n?', '', content, flags=re.DOTALL)
    content = re.sub(r'\{success && <div.*?>.*?\{success\}.*?</div>\}\n?', '', content, flags=re.DOTALL)
    content = re.sub(r'\{ventaExitosa && <div.*?>.*?\{ventaExitosa\}.*?</div>\}\n?', '', content, flags=re.DOTALL)
    content = re.sub(r'\{error \? \(\s*<div.*?>.*?</p>\s*</div>\s*\) : \(', '{!error && (', content, flags=re.DOTALL)

    # 6. Replace loading spin with Skeleton
    content = re.sub(
        r'<div className="flex justify-center p-12"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-cyan-500"></div></div>',
        r'<TableSkeleton />',
        content
    )
    
    # Edge case alert
    content = re.sub(r'alert\((.*?)\)', r'toast.info(\1)', content)

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

print("Refactorización completa con script de Python.")
