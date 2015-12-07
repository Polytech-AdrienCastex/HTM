package htm.model.pooler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import htm.model.model.Column;
import htm.model.model.InputSynapse;

public class SpatialPooler
{
    public static Builder create()
    {
        return new Builder();
    }
    public static class Builder
    {
        public Builder()
        { }
        
        private int desiredLocalActivity = 1;
        private double connectedPermanance = 0.7;
        private double permananceDec = 0.05;
        private double permananceInc = 0.05;
        private int minOverlap = 2;
        private double inhibitionRadius;
        private int nbColumns = 10;
        private int nbSynapses = nbColumns;
        
        public Builder setDesiredLocalActivity(int value)
        {
            this.desiredLocalActivity = value;
            return this;
        }
        public Builder setConnectedPermanance(double value)
        {
            this.connectedPermanance = value;
            return this;
        }
        public Builder setPermanance(double value)
        {
            this.permananceInc = value;
            this.permananceDec = value;
            return this;
        }
        public Builder setPermananceDec(double value)
        {
            this.permananceDec = value;
            return this;
        }
        public Builder setPermananceInc(double value)
        {
            this.permananceInc = value;
            return this;
        }
        public Builder setMinOverlap(int value)
        {
            this.minOverlap = value;
            return this;
        }
        public Builder setInhibitionRadius(double value)
        {
            this.inhibitionRadius = value;
            return this;
        }
        public Builder setNbColumns(int value)
        {
            this.nbColumns = value;
            return this;
        }
        public Builder setNbSynapses(int value)
        {
            this.nbSynapses = value;
            return this;
        }
        
        public SpatialPooler build()
        {
            return new SpatialPooler(
                    nbColumns,
                    desiredLocalActivity,
                    connectedPermanance,
                    minOverlap,
                    permananceDec,
                    permananceInc,
                    nbSynapses,
                    inhibitionRadius);
        }
    }
    
    
    public SpatialPooler(
            int nbColumns,
            int desiredLocalActivity,
            double connectedPermanance,
            int minOverlap,
            double permananceDec,
            double permananceInc,
            int nbSynapses,
            double inhibitionRadius)
    {
        this.inhibitionRadius = inhibitionRadius;
        this.connectedPermanance = connectedPermanance;
        this.minOverlap = minOverlap;
        this.permananceDec = permananceDec;
        this.permananceInc = permananceInc;
        this.desiredLocalActivity = desiredLocalActivity;

        this.columns = new Column[nbColumns];

        Random random = new Random();

        for (int x = 0; x < nbColumns; x++)
        {
            List<InputSynapse> synapses = new ArrayList<>(nbSynapses);
            
            for (int i = 0; i < nbSynapses; i++)
                synapses.add(new InputSynapse(i, random.nextInt((int)(connectedPermanance * 10)) / 10.0));
            
            columns[x] = new Column(synapses);
        }
    }
    
    private Column[] columns;
    
    private final int desiredLocalActivity;
    private final double connectedPermanance;
    private final double permananceDec;
    private final double permananceInc;
    private final int minOverlap;
    private double inhibitionRadius;
    private double inhibitionRadiusBefore = 0.0;
    private boolean learning = true;
    
    public boolean isConnected(InputSynapse s)
    {
        return s.isConnected(connectedPermanance);
    }
    
    
    public boolean isLearning()
    {
        return learning;
    }
    public void setLearning(boolean learning)
    {
        this.learning = learning;
    }

    public Column[] getColumns()
    {
        return columns;
    }

    public void setInputs(int[] inputs)
    {
        Stream.of(columns)
                .map(Column::getSynapses)
                .flatMap(Collection::stream)
                .forEach(s -> s.setInputValue(inputs[s.getInputIndex()]));
    }
    public void setInputs(Integer[] inputs)
    {
        Stream.of(columns)
                .map(Column::getSynapses)
                .flatMap(Collection::stream)
                .forEach(s -> s.setInputValue(inputs[s.getInputIndex()]));
    }

    /**
     * Phase 1 : Overlap
     */
    public void computeOverlap()
    {
        for(Column column : columns)
        {
            double overlap = column.getConnectedSynapses(connectedPermanance).stream()
                    .mapToInt(InputSynapse::getInputValue)
                    .sum();
            
            if (overlap < minOverlap)
                overlap = 0;
            
            //System.out.println(overlap);
            column.setOverlap(overlap * column.getBoost(), minOverlap);
            
            //System.out.println(column.getOverlap());
        }
    }
    
    public boolean shouldBeActiveInput(Column column, int index)
    {
        LinkedList<int[]> ints = new LinkedList<>();
        for(Column c : columns)
        {
            int[] vs = new int[getColumns().length];
            
            c.getConnectedSynapses(connectedPermanance)
                    .stream()
                    .mapToInt(InputSynapse::getInputIndex)
                    .forEach(i -> vs[i] = 1);
            
            ints.add(vs);
        }
        
        int[] vs = new int[getColumns().length];
        for(int i = 0; i < vs.length; i++)
            vs[i] = 1;
        
        for(int[] is : ints)
        {
            for(int i = 0; i < vs.length; i++)
                vs[i] = vs[i] == 1 && is[i] == 1 ? 1 : 0;
        }
        
        return vs[index] == 1;
        
        //return column.getSynapses().stream().anyMatch(s -> this.isConnected(s) && s.getInputIndex() == index);
        /*
        
                                if(s.getInputIndex() == xx && sp.isConnected(s) && s.getInputValue() >= 1)
        
        double overlap = column.getConnectedSynapses(connectedPermanance).stream()
                .mapToInt(InputSynapse::getInputIndex)
                .map(i -> inputs[i])
                .sum();

        if (overlap < minOverlap)
            overlap = 0;

        List<Column> neigbors = getNeigbors(column, columns, inhibitionRadius);

        double minimalLocalActivity = kthScore(neigbors, desiredLocalActivity);
        
        return overlap >= minimalLocalActivity;*/
    }

    /**
     * Phase 2 : Inhibition
     */
    public void computeInhibition()
    {
        for(Column column : columns)
        {
            if (Math.round(inhibitionRadius) != Math.round(inhibitionRadiusBefore) || column.getNeighbors() == null)
                column.setNeighbors(getNeigbors(column, columns, inhibitionRadius));
            
            double minimalLocalActivity = kthScore(column.getNeighbors(), desiredLocalActivity);
            
            //System.out.println(column.getNeighbors().size());
            //System.out.println(minimalLocalActivity);
            
            column.setMinimalLocalActivity(minimalLocalActivity);
            column.setActive(column.getOverlap() >= minimalLocalActivity);
            
            //System.out.println(column.getActiveDutyCycle());
        }
        
        //System.out.println(activeColumns.size());
    }

    /**
     * Phase 3 : Learning
     */
    public void updateSynapses()
    {
        if(!isLearning())
            return;
        
        Stream.of(columns)
                .filter(Column::isActive)
                .map(Column::getSynapses)
                .flatMap(Collection::stream)
                .forEach(s -> s.incPermanance(s.getInputValue() == 1 ? permananceInc : -permananceDec));
        
        Collection<Integer> radiuses = new LinkedList<>();
        
        for(int i = 0; i < columns.length; i++)
        {
            final int x = i;
            Column column = columns[i];
            
            double minDutyCycle = 0.01 * getMaxDutyCycle(column.getNeighbors());
            //System.out.println(minDutyCycle);
            
            column.computeBoost(minDutyCycle);
            
            //System.out.println(column.getOverlapDutyCycle());
            if (column.getOverlapDutyCycle() < minDutyCycle)
                column.increasePermanances(0.1 * connectedPermanance);
            
            column.getConnectedSynapses(connectedPermanance)
                    .stream()
                    .map(InputSynapse::getInputIndex)
                    .map(v -> x - v)
                    .map(Math::abs)
                    .forEach(radiuses::add);
        }
        
        this.inhibitionRadiusBefore = inhibitionRadius;
        this.inhibitionRadius = averageReceptiveFieldSize(radiuses);
        System.out.println(inhibitionRadius);
    }
    
    public double getInhibitionRadius()
    {
        return inhibitionRadius;
    }
    
    protected static double averageReceptiveFieldSize(Collection<Integer> radiuses)
    {
        return radiuses.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(1.0);
    }

    protected static double getMaxDutyCycle(Collection<Column> neighbors)
    {
        return neighbors.stream()
                .max((c1, c2) -> Double.compare(c1.getActiveDutyCycle(), c2.getActiveDutyCycle()))
                .get()
                .getActiveDutyCycle();
    }

    protected static double kthScore(List<Column> neighbors, int disiredLocalActivity)
    {
        neighbors.sort((c1, c2) -> -Double.compare(c1.getOverlap(), c2.getOverlap()));
        return neighbors.get(Math.min(disiredLocalActivity, neighbors.size() - 1)).getOverlap();
    }

    protected static List<Column> getNeigbors(Column c, Column[] cs, double inhibitionRadius)
    {
        int inhib = (int)Math.round(inhibitionRadius);
        int location = Arrays.asList(cs).indexOf(c);
        return IntStream.range(
                Math.max(0, Math.min(cs.length, location - inhib)),
                Math.max(0, Math.min(cs.length, location + inhib + 1)))
                .mapToObj(i -> cs[i])
                .filter(x -> x != c)
                .collect(Collectors.toList());
    }

    public void process(Integer[] in)
    {
        this.setInputs(in);
        this.computeOverlap();
        this.computeInhibition();
        this.updateSynapses();
    }
    public void process(int[] in)
    {
        this.setInputs(in);
        this.computeOverlap();
        this.computeInhibition();
        this.updateSynapses();
    }
}
