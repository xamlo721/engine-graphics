package com.xamlo.core.engine.graphics.components;


import org.joml.Matrix4f;


/**
 * Объём видимости кадра для culling статической геометрии сцены.
 *
 * <p>Хранит скопированные элементы world→clip преобразования текущего кадра и
 * оценивает ящики прямым скалярным пересчётом clip-координат их углов вместо
 * аналитического извлечения шести плоскостей. Отказ от классических формул
 * Gribb-Hartmann зафиксирован диагностикой 2026-11-01: в собранном vintage JOML
 * 1.9.9 публичные аксессоры матрицы возвращают элементы, при которых «строка» по
 * первым индексам является фактическим столбцом математической матрицы, поэтому
 * формулы, собранные против них без учёта этого факта, берут константы плоскостей
 * из неверных элементов и дают ложное отсечение, зависящее от направления взгляда
 * (симптом на стороне клиента: мир исчезал при повороте камеры на отдельные
 * стороны света). Прямой corner-test не зависит ни от раскладки хранения, ни от
 * знака глубины в конкретной упаковке перспективного делителя: боковые границы
 * симметричны по осям X/Y, а отсекается ящик только когда все восемь его углов
 * единообразно выходят за одну границу; смешанный рисунок знаков трактуется как
 * неопределённость и оставляет геометрию на отрисовку.</p>
 */
public class Frustum {

    /** Сырое хранилище значений аксессоров mNN(i,j) в порядке заполнения i-major. */
    private final float[] mem = new float[16];
    private boolean haveMvp;

    // scratch-поля пересчёта clip-вектора угла без аллокаций в горячем цикле culling
    private double cx, cy, cz, cw;

    public void calculateFrustum(Matrix4f clipMatrix) {
        if (clipMatrix == null) {
            haveMvp = false;
            return;
        }
        int s = 0;
        mem[s++] = clipMatrix.m00(); mem[s++] = clipMatrix.m01(); mem[s++] = clipMatrix.m02(); mem[s++] = clipMatrix.m03();
        mem[s++] = clipMatrix.m10(); mem[s++] = clipMatrix.m11(); mem[s++] = clipMatrix.m12(); mem[s++] = clipMatrix.m13();
        mem[s++] = clipMatrix.m20(); mem[s++] = clipMatrix.m21(); mem[s++] = clipMatrix.m22(); mem[s++] = clipMatrix.m23();
        mem[s++] = clipMatrix.m30(); mem[s++] = clipMatrix.m31(); mem[s++] = clipMatrix.m32(); mem[s++] = clipMatrix.m33();
        haveMvp = true;
    }

    /**
     * Пересчёт мировой точки в clip-координаты прямым умножением по скопированным
     * элементам. Строка r математической матрицы восстанавливается из слотов как
     {@code sum_c mem[c*4+r]*p_c + mem[12+r]} — побитово сверено против
     {@link Matrix4f#transform} на собранном vintage (диагностический прогон
     Compare.java, 2026-11-01: расхождение ноль на всех проверенных точках).
     */
    private void toClip(float px, float py, float pz) {
        cx = mem[0] * px + mem[4] * py + mem[8] * pz + mem[12];
        cy = mem[1] * px + mem[5] * py + mem[9] * pz + mem[13];
        cz = mem[2] * px + mem[6] * py + mem[10] * pz + mem[14];
        cw = mem[3] * px + mem[7] * py + mem[11] * pz + mem[15];
    }

    /**
     * Консервативный тест ящика [x1..x2]×[y1..y2]×[z1..z2]: по умолчанию рисуем.
     * Ящик отсекается только когда все восемь его углов единообразно выходят за одну из
     * четырёх боковых границ NDC (симметричные пары X/Y), либо когда каждый угол
     * оказывается строго по невидимую сторону плоскости камеры (единозначная сторона
     * w для всех углов; знак «невидимой» стороны сверён эмпирически с фактическим
     * направлением взгляда на собранном клиенте). Кадр без поставленного
     * преобразования всегда возвращает true — culling деградирует в off.
     */
    public boolean cubeIntoFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        if (!haveMvp) {
            return true;
        }
        int outLeft = 0, outRight = 0, outBottom = 0, outTop = 0, negW = 0, evaluated = 0;
        for (int k = 0; k < 8; k++) {
            float px = ((k & 1) == 0) ? x1 : x2;
            float py = (((k >> 1) & 1) == 0) ? y1 : y2;
            float pz = (((k >> 2) & 1) == 0) ? z1 : z2;
            toClip(px, py, pz);
            if (Math.abs(cw) < 1e-5) {
                continue; // угол degenerates относительно камеры — не участвует в решении об отсечении
            }
            evaluated++;
            double nx = cx / cw;
            double ny = cy / cw;
            if (nx <= -1.0001d) outLeft++;
            if (nx >= +1.0001d) outRight++;
            if (ny <= -1.0001d) outBottom++;
            if (ny >= +1.0001d) outTop++;
            if (cw < 0) negW++;
        }
        boolean culled = false;
        if (evaluated == 8) {
            culled = (outLeft == 8) || (outRight == 8) || (outBottom == 8) || (outTop == 8) || (negW == 8);
        }
        return !culled;
    }

    /** Точка как вырожденный ящик: рисуется, пока не исключена тем же консервативным тестом. */
    public boolean pointInFrustum(float x, float y, float z) {
        return cubeIntoFrustum(x, y, z, x, y, z);
    }

    /** Сфера аппроксимируется её ограничивающим кубом — консервативно по определению. */
    public boolean sphereInFrustum(float x, float y, float z, float radius) {
        return cubeIntoFrustum(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
    }
}
