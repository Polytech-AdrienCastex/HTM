package htm.model.model;

public abstract class ISynapse
{
    public ISynapse(double initialPermanance)
    {
        this.permanance = initialPermanance;
    }
    
    private double permanance;
    
    public boolean isConnected(double connectedPermanance)
    {
        return this.permanance >= connectedPermanance;
    }
    
    public double getPermanance()
    {
        return this.permanance;
    }
    
    public void incPermanance(double value)
    {
        setPermanance(getPermanance() + value);
    }
    public void setPermanance(double d)
    {
        this.permanance = Math.min(Math.max(d, 0.0), 1.0);
    }
}
