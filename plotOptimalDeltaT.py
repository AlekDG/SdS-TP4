import numpy as np
import matplotlib.pyplot as plt

# Parámetros
delta_ts = [10.0, 5.0, 1.0, 0.1, 0.05, 0.01]
integradores = ['Gear','Beeman', 'Verlet']

# Loop sobre integradores
for integrador in integradores:
    plt.figure(figsize=(8,5))

    for dt in delta_ts:
        # Nombre del archivo según tu formato
        filename = f"optimalDeltaT{integrador}Energy{dt}.txt"

        # Leer archivo: columnas -> tiempo, error relativo
        data = np.loadtxt(filename, delimiter=',')
        t = data[:,0]
        error_rel = data[:,1]

        # Graficar
        plt.plot(t, error_rel, label=f"Δt = {dt}")

    plt.title(f"Error relativo vs Tiempo - {integrador}")
    plt.xlabel("Tiempo")
    plt.ylabel("Error relativo |E(t)-E0|/E0")
    plt.yscale('log')  # Logarítmica para comparar mejor errores pequeños y grandes
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.show()
