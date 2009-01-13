/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package org.yaml.snakeyaml.constructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.yaml.snakeyaml.Dumper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Util;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

public class ListErasureTest extends TestCase {

    public void testDefaultRepresenter() throws IOException {
        Car car = new Car();
        car.setPlate("12-XP-F4");
        List<Wheel> wheels = new LinkedList<Wheel>();
        for (int i = 1; i < 6; i++) {
            Wheel wheel = new Wheel();
            wheel.setId(i);
            wheels.add(wheel);
        }
        car.setWheels(wheels);
        assertEquals(Util.getLocalResource("constructor/car-with-tags.yaml"), new Yaml().dump(car));
    }

    public void testDumpClassTag() throws IOException {
        Car car = new Car();
        car.setPlate("12-XP-F4");
        List<Wheel> wheels = new LinkedList<Wheel>();
        for (int i = 1; i < 6; i++) {
            Wheel wheel = new Wheel();
            wheel.setId(i);
            wheels.add(wheel);
        }
        car.setWheels(wheels);
        Representer representer = new MyRepresenter();
        representer.addClassTag(Car.class, "!car");
        Dumper dumper = new Dumper(representer, new DumperOptions());
        Yaml yaml = new Yaml(dumper);
        String output = yaml.dump(car);
        assertEquals(Util.getLocalResource("constructor/car-without-tags.yaml"), output);
    }

    public void testLoadUnknounClassTag() throws IOException {
        try {
            Yaml yaml = new Yaml();
            yaml.load(Util.getLocalResource("constructor/car-without-tags.yaml"));
            fail("Must fail because of unknown tag: !car");
        } catch (YAMLException e) {
            assertEquals("Unknown tag: !car", e.getMessage());
        }

    }

    private class MyRepresenter extends Representer {
        Represent defaultRepresenter;

        public MyRepresenter() {
            super(null, Boolean.FALSE);
            defaultRepresenter = this.representers.get(null);
            this.representers.put(null, new RepresentCar());
        }

        private class RepresentCar implements Represent {
            @SuppressWarnings("unchecked")
            public Node representData(Object data) {
                if (data instanceof Wheel) {
                    Wheel wheel = (Wheel) data;
                    Map values = new HashMap();
                    values.put("id", wheel.getId());
                    return representMapping("tag:yaml.org,2002:map", values, null);
                } else {
                    return defaultRepresenter.representData(data);
                }
            }
        }

    }
}
