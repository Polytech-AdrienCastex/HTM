package htm.model.model;

public class InputSynapse extends ISynapse
{
    public InputSynapse(int inputIndex, double initialPermanance)
    {
        super(initialPermanance);
        
        this.inputIndex = inputIndex;
    }
    
    private final int inputIndex;
    private int inputValue;

    public int getInputValue()
    {
        return inputValue;
    }

    public void setInputValue(int inputValue)
    {
        this.inputValue = inputValue;
    }

    public int getInputIndex()
    {
        return inputIndex;
    }
}
