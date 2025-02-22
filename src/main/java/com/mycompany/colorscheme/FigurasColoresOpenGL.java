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
                    selectFigure((float) xPos[0], (float) yPos[0]); // Asegúrate de acceder al índice 0
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
        // Crear una ventana de ImGui
        ImGui.begin("Paleta de colores");

        // Mostrar la paleta de colores solo si hay una figura seleccionada
        if (selectedFigure != null) {
            if (ImGui.colorPicker3("Color", selectedFigure.getColor())) {
                // El color ha sido cambiado por el usuario
            }
        }

        // Botones para seleccionar el esquema de color
        if (ImGui.button("RGB")) {
            selectedScheme = "RGB";
            updateSelectedFigureColor();
        }
        ImGui.sameLine();
        if (ImGui.button("CMYK")) {
            selectedScheme = "CMYK";
            updateSelectedFigureColor();
        }
        ImGui.sameLine();
        if (ImGui.button("HSL")) {
            selectedScheme = "HSL";
            updateSelectedFigureColor();
        }
        ImGui.sameLine();
        if (ImGui.button("HSV")) {
            selectedScheme = "HSV";
            updateSelectedFigureColor();
        }

        ImGui.end();
    }

    private void updateSelectedFigureColor() {
        if (selectedFigure != null) {
            // Aquí puedes convertir el esquema de color seleccionado a RGB
            // Por simplicidad, usamos el color actual de ImGui
            selectedFigure.setColor(selectedFigure.getColor());
        }
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

        public Figure(String name, float[] shape, float[] color) {
            this.name = name;
            this.shape = shape;
            this.color = color;
        }

        public void draw() {
            if (shape.length == 4) { // Rectángulo
                drawRectangle(shape[0], shape[1], shape[2], shape[3], color);
            } else if (shape.length == 3) { // Círculo
                drawCircle(shape[0], shape[1], shape[2], color);
            } else if (shape.length == 6) { // Triángulo
                drawTriangle(shape[0], shape[1], shape[2], shape[3], shape[4], shape[5], color);
            }
        }

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