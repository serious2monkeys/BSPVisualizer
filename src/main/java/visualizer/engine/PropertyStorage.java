/**
 * PropertyStorage.java
 * <p>
 * Copyright 2014-2014 Michael Hoffer <info@michaelhoffer.de>. All rights
 * reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info@michaelhoffer.de>.
 */
package visualizer.engine;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Хранилише свойств
 */
public class PropertyStorage {

    private final Map<String, Object> map = new HashMap<>();

    private static final Color[] colors = {
            Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA,
            Color.WHITE, Color.BLACK, Color.GRAY, Color.ORANGE};

    public PropertyStorage() {
        randomColor(this);
    }

    public void set(String key, Object property) {
        map.put(key, property);
    }

    public <T> Optional<T> getValue(String key) {

        Object value = map.get(key);

        try {
            return Optional.ofNullable((T) value);
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    public void delete(String key) {
        map.remove(key);
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    static void randomColor(PropertyStorage storage) {
        Color c = colors[(int) (Math.random() * colors.length)];

        storage.set("material:color",
                "" + c.getRed()
                        + " " + c.getGreen()
                        + " " + c.getBlue());
    }
}
