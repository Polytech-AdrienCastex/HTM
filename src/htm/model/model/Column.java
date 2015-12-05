package htm.model.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Column
{
    private static final int MAX_AVG_NB = 200;

    public Column(List<InputSynapse> synapses)
    {
        this.activeTimes = new LinkedList<>();
        this.goodOverlaps = new LinkedList<>();
        
        this.synapses = synapses;
        this.boost = 1.0;
    }
    
    private final LinkedList<Boolean> activeTimes;
    private final LinkedList<Boolean> goodOverlaps;
    
    private final Collection<InputSynapse> synapses;
    private List<Column> neighbors;
    
    private double activeDutyCycle;
    private double overlapDutyCycle;
    private double boost;
    private double overlap;
    private boolean isActive;
    
    
    public void computeBoost(double minDesiredDutyCycle)
    {
        if (this.activeDutyCycle > minDesiredDutyCycle)
            this.boost = 1.0;
        else
            this.boost += minDesiredDutyCycle;
    }

    public double getOverlapDutyCycle()
    {
        return overlapDutyCycle;
    }

    public double getActiveDutyCycle()
    {
        return activeDutyCycle;
    }

    public double getOverlap()
    {
        return overlap;
    }
    public void setOverlap(double overlap, double minOverlap)
    {
        this.overlap = overlap;
        this.goodOverlaps.add(overlap >= minOverlap);
        
        while(goodOverlaps.size() > MAX_AVG_NB)
            goodOverlaps.removeFirst();
        
        this.overlapDutyCycle = goodOverlaps.stream()
                .mapToInt(b -> b ? 1 : 0)
                .average()
                .getAsDouble();
    }

    public Collection<InputSynapse> getSynapses()
    {
        return synapses;
    }

    public List<Column> getNeighbors()
    {
        return neighbors;
    }

    public void setNeighbors(List<Column> neighbors)
    {
        this.neighbors = neighbors;
    }

    public Collection<InputSynapse> getConnectedSynapses(double connectedPermanance)
    {
        return synapses.stream()
                .filter(s -> s.isConnected(connectedPermanance))
                .collect(Collectors.toList());
    }

    public double getBoost()
    {
        return boost;
    }

    public void increasePermanances(double value)
    {
        synapses.stream()
                .forEach(s -> s.incPermanance(value));
    }

    public void setActive(boolean active)
    {
        this.isActive = active;
        this.activeTimes.add(active);
        
        while(activeTimes.size() > MAX_AVG_NB)
            activeTimes.removeFirst();
        
        this.activeDutyCycle = activeTimes.stream()
                .mapToInt(b -> b ? 1 : 0)
                .average()
                .getAsDouble();
    }
    public boolean isActive()
    {
        return this.isActive;
    }

    private double minimalLocalActivity = 0.0;
    public double getMinimalLocalActivity()
    {
        return minimalLocalActivity;
    }
    public void setMinimalLocalActivity(double minimalLocalActivity)
    {
        this.minimalLocalActivity = minimalLocalActivity;
    }
}
