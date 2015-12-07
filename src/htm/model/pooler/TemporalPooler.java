package htm.model.pooler;

import htm.model.model.Column;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemporalPooler
{
    
    private Map<Cell, Column> cols = new HashMap<>();
    private Map<Column, Cell> cells = new HashMap<>();
    private Collection<Link> links = new LinkedList<>();
    private Map<Column, Boolean> oldValues = new HashMap<>();
    
    private static class Link
    {
        public Link(Cell from, Cell to)
        {
            this.from = from;
            this.to = to;
            this.isRejected = false;
            this.isPredictive = false;
            this.connectivity = 0.0;
        }
        
        public final Cell from;
        public final Cell to;
        
        public boolean isRejected;
        public boolean isPredictive;
        
        public double rejectivity = 0.0;
        public void setRejectivity(double value)
        {
            this.rejectivity = Math.max(0, Math.min(1.0, value));
        }
        public boolean getRejectivity()
        {
            return this.rejectivity >= 0.7;
        }
        
        public double connectivity;
        public void setConnectivity(double value)
        {
            this.connectivity = Math.max(0, Math.min(1.0, value));
        }
        public boolean isConnected(double minValue)
        {
            return this.connectivity >= minValue;
        }
    }
        
    public static int NOW = 1;
    public static int BEFORE = 0;

    public static int ROOT = 0;
    public static int NOT_ROOT = 1;
    
    private class Cell
    {
        public Cell(Column parent, int type)
        {
            this.parent = parent;
            this.type = type;
        }
        
        public final int type;
        
        public final Column parent;
        public Column getColumn()
        {
            return parent;
        }
        
        private boolean[] active = new boolean[]
        {
            false,
            false
        };
        public boolean isActive(int time)
        {
            return active[time];
        }
        public void setActive(boolean value)
        {
            active[0] = active[1];
            active[1] = value;
        }
    }
    
    public TemporalPooler(SpatialPooler sp)
    {
        this.sp = sp;
    }
    
    private final SpatialPooler sp;
    
    private int learning = 1;
    
    protected Collection<Cell> getNeigbors(Column[] cs, Cell c)
    {
        final List<Column> csList = Arrays.asList(cs);
        
        return Stream.of(cs)
                .map(cells::get)
                .filter(cx -> cx != c)
                .filter(cx -> Math.abs(csList.indexOf(cx.getColumn()) - csList.indexOf(c.getColumn())) <= 3)
                .collect(Collectors.toList());
    }
    
    public void run(Column[] cs)
    {
        if(cells.isEmpty())
        {
            Stream.of(cs)
                    .map(c -> new Cell(c, 0))
                    .peek(cell -> cells.put(cell.getColumn(), cell))
                    .forEach(cell -> cols.put(cell, cell.getColumn()));
        }
        
        for(Column c : cs)
        {
            Cell cell = cells.get(c);
            /*
            getNeigbors(cs, cell)
                    .stream()
                    .filter(cx -> links.stream().noneMatch(l -> l.to.equals(cx) && l.to.equals(cell)))
                    .forEach(cx -> links.add(new Link(cx, cell)));*/
            
            Stream.of(cs)
                    .filter(cx -> cx != c)
                    .map(cells::get)
                    .filter(cx -> links.stream().noneMatch(l -> l.to.equals(cx) && l.to.equals(cell)))
                    .forEach(cx -> links.add(new Link(cx, cell)));
        }
        
        if(learning <= 0)
            updateLinks();
        else
            learning--;
        
        updateOldValues(cs);
    }
    
    public String getString(Column c)
    {
        return "";/*
        return links.stream()
                .map(l -> l.connectivity)
                .map(Object::toString)
                .reduce("", (s1,s2) -> s1 + " " + s2);*/
    }
    public boolean isPreditive(Column c)
    {
        return isPreditive(cells.get(c));
    }
    public boolean isPreditive(Cell c)
    {/*
        return links.stream()
                .filter(l -> l.to == c)
                .anyMatch(l -> l.isConnected(0.7));*/
        
        return links.stream()
                .filter(l -> l.to == c)
                .anyMatch(l -> !l.getRejectivity() && l.isPredictive && isActive(l, NOW));
    }
    
    
    protected boolean isActive(Link l, int time)
    {
        if(l.from.type == 0)
        {
            if(time == NOW)
                return l.from.getColumn().isActive();
            else
                return oldValues.get(cols.get(l.from));
        }
        else
        {
            return l.from.isActive(time); // Maybe
        }
    }
    
    public void init(Column[] cs)
    {
        Stream.of(cs)
                .forEach(c -> oldValues.put(c, c.isActive()));
    }
    protected void updateOldValues(Column[] cs)
    {
        Stream.of(cs)
                .forEach(c -> oldValues.put(c, c.isActive()));
    }
    protected void updateLinks()
    {
        /*
        C  /  TB
        0     0     -
        0     1     Remove
        1     0     -
        1     1     Predictive
        */
        /*
        links.stream()
                .filter(l -> oldValues.get(cols.get(l.from)))
                .peek(l -> l.isPredictive = cols.get(l.to).isActive())
                .forEach(l -> l.isRejected = !cols.get(l.to).isActive());*/
        
        links.stream()
                .filter(l -> l.isPredictive && !isActive(l, NOW))
                .peek(l -> l.isPredictive = false)
                .peek(l -> l.isRejected = true)
                .peek(l -> l.setConnectivity(0))
                .forEach(l -> l.setRejectivity(1));
        
        links.stream()
                .filter(l -> isActive(l, BEFORE))
                .peek(l -> l.isPredictive = cols.get(l.to).isActive())
                .peek(l -> l.isRejected = l.isRejected || !cols.get(l.to).isActive())
                .peek(l -> l.setRejectivity(l.rejectivity + (l.isRejected ? 0.5 : -0.1)))
                .peek(l -> l.setConnectivity(l.connectivity + (l.isPredictive && !l.isRejected ? 0.1 : -0.1)))
                .forEach(x -> {});
    }
}
