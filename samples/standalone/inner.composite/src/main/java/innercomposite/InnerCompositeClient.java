package innercomposite;

import org.osoa.sca.CompositeContext;
import org.osoa.sca.CurrentCompositeContext;

/**
 * Simple client program that invokes the components that we wired together.
 *
 * @version $Rev$ $Date$
 */
public class InnerCompositeClient {

    public static void main(String[] args) throws Exception {
        CompositeContext context = CurrentCompositeContext.getContext();

        Source source = context.locateService(Source.class, "SourceComponent");
        System.out.println("Main thread " + Thread.currentThread());
        source.clientMethod("Client.main");
        Thread.sleep(500);
    }
}
