Desarrolla un plugin para Minecraft versión 1.21.x (especialmente compatible con 1.21.11) que gestione diferentes profesiones medievales: Caballero, Herrero, Alquimista, Doctor, Comerciante y Tabernero.



Cada profesión debe obtenerse mediante la realización de diversas misiones o clases, las cuales solo se pueden completar dentro de una zona delimitada por WorldGuard.



Detalles específicos por profesión:



\- Herrero: Debe completar un curso que incluya aprendizaje y uso de mesas de herrería, calderos con lava, yunques, etc. Al completar el curso, obtiene el rango de aprendiz de herrero, con la posibilidad de fabricar objetos, pero con un 40% de probabilidad de fallo que cause la ruptura del ítem.



\- Doctor: Debe realizar un curso para obtener su rango, ganando habilidades adicionales como la capacidad de teletransportarse a jugadores enfermos o heridos. Solo puede usar el "brewery stand" para crear pociones de curación.



\- Alquimista: Puede usar el "brewery stand" para fabricar pociones, incluyendo de curación, pero sin el conocimiento médico del doctor, estas pociones tienen un 60% de probabilidad de salir mal o causar efectos negativos.



\- Tabernero: Usando el plugin BreweryX, puede usar el "brewery stand" exclusivamente para fabricar bebidas alcohólicas relacionadas con ese plugin.



\- Caballero y Comerciante: Proponer funciones y misiones específicas para estas profesiones que encajen con la temática medieval y complementen las mecánicas del plugin.



Instrucciones de desarrollo:



1\. Implementar un sistema de misiones o clases que restrinja su ejecución a zonas definidas mediante WorldGuard.

2\. Crear un sistema de rangos y habilidades por profesión con los detalles mencionados.

3\. Integrar las funcionalidades con plugins existentes como BreweryX para el tabernero.

4\. Proponer ideas innovadoras para Caballero y Comerciante que aporten roles únicos y atractivos.

5\. Asegurar que las mecánicas de éxito/fracaso en las acciones (herrero y alquimista) se manejen con las probabilidades indicadas.



\# Output Format



Devuelve un esquema detallado o un plan para el desarrollo del plugin, incluyendo estructura, funcionalidades clave, posibles comandos o API a usar, integración con WorldGuard y BreweryX, y las ideas propuestas para las profesiones de Caballero y Comerciante.



\# Notes



Evitar conflictos o interferencias entre los diferentes roles y sus permisos especialmente en el uso del "brewery stand".



Incluye mecanismos para gestionar el progreso y logros dentro de cada profesión.

