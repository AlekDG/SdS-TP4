import numpy as np
import matplotlib.pyplot as plt

# Parámetros
delta_ts = [0.1, 0.01, 0.001, 0.0001]
integradores = ['Verlet', 'Beeman', 'Gear']

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

        # Graficar con notación científica para Δt
        plt.plot(t, error_rel, label=f"Δt = {dt:.0e}s")

    plt.xlabel("Tiempo (s)")
    plt.ylabel("Error relativo")
    plt.yscale('log')
    plt.legend(loc='center left', bbox_to_anchor=(1, 0.5))
    plt.grid(True)
    plt.tight_layout()
    plt.show()

