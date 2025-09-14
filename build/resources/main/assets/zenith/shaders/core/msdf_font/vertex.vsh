#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 TexCoord;
out vec4 FragColor;
out vec2 GlobalPos; // Передаем глобальные координаты

void main() {
    TexCoord = UV0;
    FragColor = Color;
    GlobalPos = Position.xy; // Передаем позицию вершины в мировых координатах

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}