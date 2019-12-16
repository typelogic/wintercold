package dx.grrr;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import apdu4j.APDUBIBO;
import apdu4j.CommandAPDU;
import apdu4j.ResponseAPDU;

public class Sys extends DefaultTask {
    static public int counter = 0;
    public String msg = "HELLO";

    public Sys() {
        System.out.println("[Sys]");

        Integer value1 = null;
        Integer value2 = new Integer(10);
        Optional<Integer> b = Optional.of(value2);
    }

    @TaskAction
    public void doAction() {
        System.out.println("counter = " + counter);
        counter++;
    }

    static public int sum (Integer a, Integer b) {
        a = Preconditions.checkNotNull(a,"illegal arg");
        b = Preconditions.checkNotNull(b,"illegal arg");
        return 0;
    }
}
