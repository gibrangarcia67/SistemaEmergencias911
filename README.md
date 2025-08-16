# SistemaEmergencias911

## Descripción
**SistemaEmergencias911** es una aplicación en **Java** que simula la gestión de un centro de emergencias.
Permite registrar llamadas de incidentes, asignar operadores y coordinar unidades de respuesta como policía, bomberos y ambulancias.
Incluye una interfaz gráfica (**GUI**) para facilitar la interacción del usuario.

## Estructura del proyecto
- `src/` → Código fuente en Java
  - `Main.java` → Clase principal que ejecuta la aplicación
  - `EmergenciasGUI.java` → Interfaz gráfica del sistema
  - `Operador.java` → Clase que modela a los operadores del centro de atención
  - `UnidadEmergencia.java` → Clase que representa los vehículos/unidades de respuesta
  - `Llamada.java` → Clase para gestionar la información de llamadas recibidas
  - `SistemaEmergencias.java` → Lógica central del sistema
- `README.md` → Documentación del proyecto

## Tecnologías utilizadas
- **Lenguaje:** Java
- **Paradigma:** Programación Orientada a Objetos (POO)
- **Interfaz gráfica:** Swing (javax.swing)

## Requisitos
- **Java JDK 8+**
- IDE recomendado: IntelliJ IDEA, Eclipse o NetBeans

## Instalación y ejecución
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/usuario/SistemaEmergencias911.git
