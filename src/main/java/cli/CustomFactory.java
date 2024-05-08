package cli;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.util.Arrays;
import java.util.List;

public class CustomFactory implements IFactory {

    private final IFactory factory = CommandLine.defaultFactory();
    private final List<Object> instances;

    public CustomFactory(Object... instances) {
        this.instances = Arrays.asList(instances);
    }

    public <K> K create(Class<K> cls) throws Exception {
        for(Object obj : instances) {
            if(cls.isAssignableFrom(obj.getClass())) {
                return cls.cast(obj);
            }
        }
        return factory.create(cls);
    }
}
