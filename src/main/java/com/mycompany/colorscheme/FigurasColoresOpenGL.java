package com.mycompany.colorscheme;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.util.ArrayList;
import java.util.List;

public class FigurasColoresOpenGL {

    private long window;
    private int width = 800;
    private int height = 600;
    private String selectedScheme = "RGB";

    // ImGui
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    // Figuras
    private List<Figure> figures = new ArrayList<>();
    private Figure selectedFigure = null;

    public void run() {
        try {
            init();
            loop();
        } finally {
            // Liberar recursos
            imGuiGl3.dispose();
            imGuiGlfw.dispose();
            ImGui.destroyContext();
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
        }
    }

    private void init() {
        // Inicializar GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }
    
        // Configurar la ventana
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
    
        // Crear la ventana
        window = GLFW.glfwCreateWindow(width, height, "Esquemas de colores aplicado a figuras", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("No se pudo crear la ventana GLFW");
        }
    
        // Centrar la ventana
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
    
        // Maximizar la ventana
        GLFW.glfwMaximizeWindow(window);
    
        // Configurar callbacks
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                FigurasColoresOpenGL.this.width = width;
                FigurasColoresOpenGL.this.height = height;
                GL30.glViewport(0, 0, width, height);
            }
        });
    
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                    GLFW.glfwSetWindowShouldClose(window, true);
                }
            }
        });
    
        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(window, xPos, yPos);
                    selectFigure((float) xPos[0], (float) yPos[0]);
                }
            }
        });
    
        // Hacer la ventana visible
        GLFW.glfwShowWindow(window);
    
        // Crear el contexto OpenGL
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
    
        // Habilitar VSync
        GLFW.glfwSwapInterval(1);
    
        // Configurar el viewport
        GL30.glViewport(0, 0, width, height);
    
        // Establecer el color de fondo (blanco)
        GL30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    
        // Inicializar ImGui
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Habilitar navegación con teclado
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 330");
    
        // Crear figuras
        figures.add(new Figure("Rectángulo", new float[]{-0.8f, -0.8f, 0.4f, 0.4f}, new float[]{0.0f, 0.0f, 0.0f}));
        figures.add(new Figure("Círculo", new float[]{0.0f, 0.0f, 0.2f}, new float[]{0.0f, 0.0f, 0.0f}));
        figures.add(new Figure("Triángulo", new float[]{0.4f, -0.8f, 0.4f, -0.4f, 0.8f, -0.8f}, new float[]{0.0f, 0.0f, 0.0f}));
    }

    private void loop() {
        // Bucle principal
        while (!GLFW.glfwWindowShouldClose(window)) {
            try {
                // Limpiar el buffer de color
                GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

                // Iniciar ImGui
                imGuiGlfw.newFrame();
                ImGui.newFrame();

                // Dibujar las figuras
                drawFigures();

                // Dibujar la interfaz de ImGui
                drawImGui();

                // Renderizar ImGui
                ImGui.render();
                imGuiGl3.renderDrawData(ImGui.getDrawData());

                // Intercambiar los buffers
                GLFW.glfwSwapBuffers(window);

                // Poll de eventos
                GLFW.glfwPollEvents();
            } catch (Exception e) {
                e.printStackTrace();
                break; // Salir del bucle si ocurre un error
            }
        }
    }

    private void drawFigures() {
        for (Figure figure : figures) {
            figure.draw();
            if (figure == selectedFigure) {
                figure.drawBorder(); // Dibujar borde si está seleccionada
            }
        }
    }

    private void drawImGui() {
        // Crear una ventana de ImGui más alargada
        ImGui.setNextWindowSize(600, 400); // Ajustar el tamaño de la ventana
        ImGui.begin("Paleta de colores y perfiles");
    
        // Mostrar la paleta de colores RGB solo si hay una figura seleccionada
        if (selectedFigure != null) {
            float[] color = selectedFigure.getColor();
    
            // Paleta de colores RGB
            ImGui.text("Paleta de colores RGB:");
            if (ImGui.colorPicker3("Color RGB", color)) {
                selectedFigure.setColor(color); // Actualizar el color de la figura
            }
    
            // Separador visual
            ImGui.separator();
    
            // Mostrar barras de desplazamiento para el perfil seleccionado
            ImGui.text("Perfil de color seleccionado: " + selectedScheme);
            switch (selectedScheme) {
                case "RGB":
                    // Barras de desplazamiento para RGB
                    float[] red = {color[0]}; // Usar un array para almacenar el valor del slider
                    float[] green = {color[1]};
                    float[] blue = {color[2]};
    
                    if (ImGui.sliderFloat("Rojo", red, 0, 1, "%.2f", 0)) {
                        color[0] = red[0]; // Actualizar el componente rojo
                        selectedFigure.setColor(color);
                    }
                    if (ImGui.sliderFloat("Verde", green, 0, 1, "%.2f", 0)) {
                        color[1] = green[0]; // Actualizar el componente verde
                        selectedFigure.setColor(color);
                    }
                    if (ImGui.sliderFloat("Azul", blue, 0, 1, "%.2f", 0)) {
                        color[2] = blue[0]; // Actualizar el componente azul
                        selectedFigure.setColor(color);
                    }
                    break;
    
                case "CMYK":
                    // Convertir RGB a CMYK
                    float[] cmyk = rgbToCmyk(color);
                    float[] cyan = {cmyk[0]};
                    float[] magenta = {cmyk[1]};
                    float[] yellow = {cmyk[2]};
                    float[] black = {cmyk[3]};
    
                    if (ImGui.sliderFloat("Cian", cyan, 0, 1, "%.2f", 0)) {
                        cmyk[0] = cyan[0]; // Actualizar solo el componente Cian
                        selectedFigure.setColor(cmykToRgb(new float[]{cmyk[0], magenta[0], yellow[0], black[0]}));
                    }
                    if (ImGui.sliderFloat("Magenta", magenta, 0, 1, "%.2f", 0)) {
                        cmyk[1] = magenta[0]; // Actualizar solo el componente Magenta
                        selectedFigure.setColor(cmykToRgb(new float[]{cyan[0], cmyk[1], yellow[0], black[0]}));
                    }
                    if (ImGui.sliderFloat("Amarillo", yellow, 0, 1, "%.2f", 0)) {
                        cmyk[2] = yellow[0]; // Actualizar solo el componente Amarillo
                        selectedFigure.setColor(cmykToRgb(new float[]{cyan[0], magenta[0], cmyk[2], black[0]}));
                    }
                    if (ImGui.sliderFloat("Negro", black, 0, 1, "%.2f", 0)) {
                        cmyk[3] = black[0]; // Actualizar solo el componente Negro
                        selectedFigure.setColor(cmykToRgb(new float[]{cyan[0], magenta[0], yellow[0], cmyk[3]}));
                    }
                    break;
    
                case "HSL":
                    // Convertir RGB a HSL
                    float[] hsl = rgbToHsl(color);
                    float[] hue = {hsl[0]};
                    float[] saturation = {hsl[1]};
                    float[] lightness = {hsl[2]};
    
                    if (ImGui.sliderFloat("Tono (H)", hue, 0, 1, "%.2f", 0)) {
                        hsl[0] = hue[0];
                        selectedFigure.setColor(hslToRgb(new float[]{hsl[0], hsl[1], hsl[2]}));
                    }
                    if (ImGui.sliderFloat("Saturación (S)", saturation, 0, 1, "%.2f", 0)) {
                        hsl[1] = saturation[0];
                        selectedFigure.setColor(hslToRgb(new float[]{hsl[0], hsl[1], hsl[2]}));
                    }
                    if (ImGui.sliderFloat("Luminosidad (L)", lightness, 0, 1, "%.2f", 0)) {
                        hsl[2] = lightness[0];
                        selectedFigure.setColor(hslToRgb(new float[]{hsl[0], hsl[1], hsl[2]}));
                    }
                    break;
    
                case "HSV":
                    // Convertir RGB a HSV
                    float[] hsv = rgbToHsv(color);
                    float[] h = {hsv[0]};
                    float[] s = {hsv[1]};
                    float[] v = {hsv[2]};
    
                    if (ImGui.sliderFloat("Tono (H)", h, 0, 1, "%.2f", 0)) {
                        hsv[0] = h[0];
                        selectedFigure.setColor(hsvToRgb(new float[]{hsv[0], hsv[1], hsv[2]}));
                    }
                    if (ImGui.sliderFloat("Saturación (S)", s, 0, 1, "%.2f", 0)) {
                        hsv[1] = s[0];
                        selectedFigure.setColor(hsvToRgb(new float[]{hsv[0], hsv[1], hsv[2]}));
                    }
                    if (ImGui.sliderFloat("Valor (V)", v, 0, 1, "%.2f", 0)) {
                        hsv[2] = v[0];
                        selectedFigure.setColor(hsvToRgb(new float[]{hsv[0], hsv[1], hsv[2]}));
                    }
                    break;
            }
    
            // Separador visual
            ImGui.separator();
    
            // Controles para transformaciones
            ImGui.text("Transformaciones:");
            if (ImGui.button("Rotación")) {
                // Activar rotación
            }
            if (ImGui.button("Escalado")) {
                // Activar escalado
            }
            if (ImGui.button("Traslación")) {
                // Activar traslación
            }
            if (ImGui.button("Sesgo (Bias)")) {
                // Activar sesgo
            }
    
            // Sliders para transformaciones
            float[] rotation = {selectedFigure.getRotation()};
            if (ImGui.sliderFloat("Rotación", rotation, -360.0f, 360.0f, "%.2f", 0)) {
                selectedFigure.setRotation(rotation[0]);
            }
    
            float[] scaleX = {selectedFigure.getScaleX()};
            float[] scaleY = {selectedFigure.getScaleY()};
            if (ImGui.sliderFloat("Escala X", scaleX, 0.1f, 2.0f, "%.2f", 0)) {
                selectedFigure.setScaleX(scaleX[0]);
            }
            if (ImGui.sliderFloat("Escala Y", scaleY, 0.1f, 2.0f, "%.2f", 0)) {
                selectedFigure.setScaleY(scaleY[0]);
            }
    
            float[] translateX = {selectedFigure.getTranslateX()};
            float[] translateY = {selectedFigure.getTranslateY()};
            if (ImGui.sliderFloat("Traslación X", translateX, -1.0f, 1.0f, "%.2f", 0)) {
                selectedFigure.setTranslateX(translateX[0]);
            }
            if (ImGui.sliderFloat("Traslación Y", translateY, -1.0f, 1.0f, "%.2f", 0)) {
                selectedFigure.setTranslateY(translateY[0]);
            }
    
            float[] bias = {selectedFigure.getBias()};
            if (ImGui.sliderFloat("Sesgo (Bias)", bias, -1.0f, 1.0f, "%.2f", 0)) {
                selectedFigure.setBias(bias[0]);
            }
        }
    
        // Botones para seleccionar el esquema de color (fuera de la paleta RGB)
        ImGui.separator();
        ImGui.text("Seleccionar perfil de color:");
        if (ImGui.button("RGB")) {
            selectedScheme = "RGB";
        }
        if (ImGui.button("CMYK")) {
            selectedScheme = "CMYK";
        }
        if (ImGui.button("HSL")) {
            selectedScheme = "HSL";
        }
        if (ImGui.button("HSV")) {
            selectedScheme = "HSV";
        }
    
        ImGui.end();
    }

    // Métodos de conversión de color (simplificados)
    private float[] rgbToCmyk(float[] rgb) {
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float k = 1 - Math.max(r, Math.max(g, b));
        float c = (1 - r - k) / (1 - k);
        float m = (1 - g - k) / (1 - k);
        float y = (1 - b - k) / (1 - k);
        return new float[]{c, m, y, k};
    }

    private float[] cmykToRgb(float[] cmyk) {
        float c = cmyk[0], m = cmyk[1], y = cmyk[2], k = cmyk[3];
        float r = (1 - c) * (1 - k);
        float g = (1 - m) * (1 - k);
        float b = (1 - y) * (1 - k);
        return new float[]{r, g, b};
    }

    private float[] rgbToHsl(float[] rgb) {
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0, s = 0, l = (max + min) / 2;

        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2 - max - min) : d / (max + min);
            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else if (max == b) {
                h = (r - g) / d + 4;
            }
            h /= 6;
        }

        return new float[]{h, s, l};
    }

    private float[] hslToRgb(float[] hsl) {
        float h = hsl[0], s = hsl[1], l = hsl[2];
        float r, g, b;

        if (s == 0) {
            r = g = b = l; // Escala de grises
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1 / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1 / 3f);
        }

        return new float[]{r, g, b};
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1 / 6f) return p + (q - p) * 6 * t;
        if (t < 1 / 2f) return q;
        if (t < 2 / 3f) return p + (q - p) * (2 / 3f - t) * 6;
        return p;
    }

    private float[] rgbToHsv(float[] rgb) {
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0, s = 0, v = max;

        float d = max - min;
        s = max == 0 ? 0 : d / max;

        if (max != min) {
            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else if (max == b) {
                h = (r - g) / d + 4;
            }
            h /= 6;
        }

        return new float[]{h, s, v};
    }

    private float[] hsvToRgb(float[] hsv) {
        float h = hsv[0], s = hsv[1], v = hsv[2];
        float r = 0, g = 0, b = 0;

        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        switch (i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
        }

        return new float[]{r, g, b};
    }

    private void selectFigure(float x, float y) {
        // Convertir coordenadas de pantalla a coordenadas de OpenGL
        float glX = (x / width) * 2 - 1;
        float glY = 1 - (y / height) * 2;

        for (Figure figure : figures) {
            if (figure.contains(glX, glY)) {
                selectedFigure = figure;
                break;
            }
        }
    }

    public static void main(String[] args) {
        new FigurasColoresOpenGL().run();
    }

    private static class Figure {
        private String name;
        private float[] shape;
        private float[] color;
    
        // Transformations
        private float rotation = 0.0f; // Rotation angle in degrees
        private float scaleX = 1.0f;   // Scaling factor for X-axis
        private float scaleY = 1.0f;   // Scaling factor for Y-axis
        private float translateX = 0.0f; // Translation along X-axis
        private float translateY = 0.0f; // Translation along Y-axis
        private float bias = 0.0f;      // Bias (offset) for the figure
    
        public Figure(String name, float[] shape, float[] color) {
            this.name = name;
            this.shape = shape;
            this.color = color;
        }
    
        // Getters and setters for transformations
        public float getRotation() { return rotation; }
        public void setRotation(float rotation) { this.rotation = rotation; }
    
        public float getScaleX() { return scaleX; }
        public void setScaleX(float scaleX) { this.scaleX = scaleX; }
    
        public float getScaleY() { return scaleY; }
        public void setScaleY(float scaleY) { this.scaleY = scaleY; }
    
        public float getTranslateX() { return translateX; }
        public void setTranslateX(float translateX) { this.translateX = translateX; }
    
        public float getTranslateY() { return translateY; }
        public void setTranslateY(float translateY) { this.translateY = translateY; }
    
        public float getBias() { return bias; }
        public void setBias(float bias) { this.bias = bias; }
    
        // Draw the figure with transformations
        public void draw() {
            GL30.glPushMatrix(); // Save the current transformation matrix
    
            // Apply transformations in the correct order
            GL30.glTranslatef(translateX, translateY, 0.0f); // Translation
    
            // Apply bias (additional translation)
            if (Math.abs(bias) <= 1.0f) { // Ensure bias is within a valid range
                GL30.glTranslatef(bias, bias, 0.0f);
            } else {
                System.err.println("Invalid bias value: " + bias);
            }
    
            // Apply rotation
            if (rotation >= -360.0f && rotation <= 360.0f) { // Ensure rotation is within a valid range
                GL30.glRotatef(rotation, 0.0f, 0.0f, 1.0f);
            } else {
                System.err.println("Invalid rotation value: " + rotation);
            }
    
            // Apply scaling
            GL30.glScalef(scaleX, scaleY, 1.0f); // Scaling
    
            // Draw the figure based on its shape
            if (shape.length == 4) { // Rectángulo
                drawRectangle(shape[0], shape[1], shape[2], shape[3], color);
            } else if (shape.length == 3) { // Círculo
                drawCircle(shape[0], shape[1], shape[2], color);
            } else if (shape.length == 6) { // Triángulo
                drawTriangle(shape[0], shape[1], shape[2], shape[3], shape[4], shape[5], color);
            }
    
            GL30.glPopMatrix(); // Restore the original transformation matrix
    
            // Check for OpenGL errors
            int error = GL30.glGetError();
            if (error != GL30.GL_NO_ERROR) {
                System.err.println("OpenGL Error: " + error);
            }
        }
    
        // Other methods (drawBorder, contains, getColor, setColor) remain unchanged
        public void drawBorder() {
            float[] borderColor = {1.0f, 0.0f, 0.0f}; // Borde rojo
            if (shape.length == 4) { // Rectángulo
                drawRectangleBorder(shape[0], shape[1], shape[2], shape[3], borderColor);
            } else if (shape.length == 3) { // Círculo
                drawCircleBorder(shape[0], shape[1], shape[2], borderColor);
            } else if (shape.length == 6) { // Triángulo
                drawTriangleBorder(shape[0], shape[1], shape[2], shape[3], shape[4], shape[5], borderColor);
            }
        }
    
        public boolean contains(float x, float y) {
            if (shape.length == 4) { // Rectángulo
                return x >= shape[0] && x <= shape[0] + shape[2] &&
                       y >= shape[1] && y <= shape[1] + shape[3];
            } else if (shape.length == 3) { // Círculo
                float dx = x - shape[0];
                float dy = y - shape[1];
                return dx * dx + dy * dy <= shape[2] * shape[2];
            } else if (shape.length == 6) { // Triángulo
                float x1 = shape[0], y1 = shape[1];
                float x2 = shape[2], y2 = shape[3];
                float x3 = shape[4], y3 = shape[5];
    
                float areaTotal = Math.abs((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1)) / 2.0f;
                float area1 = Math.abs((x1 - x) * (y2 - y) - (x2 - x) * (y1 - y)) / 2.0f;
                float area2 = Math.abs((x2 - x) * (y3 - y) - (x3 - x) * (y2 - y)) / 2.0f;
                float area3 = Math.abs((x3 - x) * (y1 - y) - (x1 - x) * (y3 - y)) / 2.0f;
    
                float areaSum = area1 + area2 + area3;
                return Math.abs(areaSum - areaTotal) < 0.0001f; // Tolerancia para errores de precisión
            }
            return false;
        }
    
        public float[] getColor() {
            return color;
        }
    
        public void setColor(float[] color) {
            this.color = color;
        }
    }

    private static void drawRectangle(float x, float y, float width, float height, float[] color) {
        GL30.glBegin(GL30.GL_QUADS);
        GL30.glColor3f(color[0], color[1], color[2]);
        GL30.glVertex2f(x, y);
        GL30.glVertex2f(x + width, y);
        GL30.glVertex2f(x + width, y + height);
        GL30.glVertex2f(x, y + height);
        GL30.glEnd();
    }

    private static void drawCircle(float x, float y, float radius, float[] color) {
        int segments = 100;
        GL30.glBegin(GL30.GL_TRIANGLE_FAN);
        GL30.glColor3f(color[0], color[1], color[2]);
        GL30.glVertex2f(x, y); // Centro del círculo
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            GL30.glVertex2f(x + (float) (Math.cos(angle) * radius), y + (float) (Math.sin(angle) * radius));
        }
        GL30.glEnd();
    }

    private static void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, float[] color) {
        GL30.glBegin(GL30.GL_TRIANGLES);
        GL30.glColor3f(color[0], color[1], color[2]);
        GL30.glVertex2f(x1, y1);
        GL30.glVertex2f(x2, y2);
        GL30.glVertex2f(x3, y3);
        GL30.glEnd();
    }

    private static void drawRectangleBorder(float x, float y, float width, float height, float[] color) {
        GL30.glBegin(GL30.GL_LINE_LOOP);
        GL30.glColor3f(color[0], color[1], color[2]);
        GL30.glVertex2f(x, y);
        GL30.glVertex2f(x + width, y);
        GL30.glVertex2f(x + width, y + height);
        GL30.glVertex2f(x, y + height);
        GL30.glEnd();
    }

    private static void drawCircleBorder(float x, float y, float radius, float[] color) {
        int segments = 100;
        GL30.glBegin(GL30.GL_LINE_LOOP);
        GL30.glColor3f(color[0], color[1], color[2]);
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            GL30.glVertex2f(x + (float) (Math.cos(angle) * radius), y + (float) (Math.sin(angle) * radius));
        }
        GL30.glEnd();
    }

    private static void drawTriangleBorder(float x1, float y1, float x2, float y2, float x3, float y3, float[] color) {
        GL30.glBegin(GL30.GL_LINE_LOOP);
        GL30.glColor3f(color[0], color[1], color[2]);
        GL30.glVertex2f(x1, y1);
        GL30.glVertex2f(x2, y2);
        GL30.glVertex2f(x3, y3);
        GL30.glEnd();
    }
}