package htm.controller;

import htm.view.IHM;
import java.io.IOException;
import htm.model.pooler.SpatialPooler;

public class Main
{
    public static void main(String [] args) throws IOException
    {
        
        SpatialPooler sp = SpatialPooler.create()
                .setNbColumns(20)
                .setNbSynapses(20)
                .setConnectedPermanance(0.7)
                .setDesiredLocalActivity(1)
                .setMinOverlap(2)
                .setPermanance(0.05)
                .setInhibitionRadius(1)
                .build();
        
        IHM ihm = new IHM(sp, 2);
        ihm.setVisible(true);
    }
}
