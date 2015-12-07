package htm.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;
import htm.model.model.Column;
import htm.model.model.InputSynapse;
import htm.model.pooler.SpatialPooler;
import htm.model.pooler.TemporalPooler;

public class IHM extends JFrame implements KeyListener, MouseInputListener
{
    public IHM(SpatialPooler sp, int nbInputs)
    {
        this.sp = sp;
        this.tp = new TemporalPooler(sp);
        
        this.addKeyListener(this);
        this.addMouseListener(this);
        
        this.setSize(1000, 600);
        
        maxInputs = Math.min(inputs.length, nbInputs);
        
        for(int i = 0; i < 1000; i++)
        {
            inputIndex++;
            sp.process(inputs[inputIndex % maxInputs]);
        }
    }
    
    private final SpatialPooler sp;
    private TemporalPooler tp;
    
    private int[][] inputs = new int[][]
    {
        { 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1 },
        { 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0 },
        { 0, 1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 1, 1 },
        { 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0 }
    };
    private final int maxInputs;
    
    int[] outputSimu = { 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1 };
    int[] inputSimu = { 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1 };
    private int inputIndex = 0;

    @Override
    public void keyTyped(KeyEvent e)
    { }

    @Override
    public void keyPressed(KeyEvent e)
    { }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            inputIndex++;
            sp.process(inputs[inputIndex % maxInputs]);
            tp.run(sp.getColumns());
            this.repaint();
        }
        
        if(e.getKeyCode() == KeyEvent.VK_A)
        {
            for(int i = 0; i < 10000; i++)
            {
                inputIndex++;
                sp.process(inputs[inputIndex % maxInputs]);
                this.repaint();
            }
        }
    }
    
    int h = 30;
    int w = 30;
    int[] ys = new int[]
    {
        100, 400, 500, 600
    };

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if(outputId != -1)
        {
            g.setColor(Color.gray);
            int ir = (int)Math.round(sp.getInhibitionRadius());
            g.fillOval(outputId * (10 + w) + 30 - (10 + w) * ir, ys[0] - h * ir, w + (10 + w) * (ir * 2), h * (1 + ir * 2));
        }
        
        for(int x = 0; x < sp.getColumns().length; x++)
        {
            Column c = sp.getColumns()[x];
            
            int cooX = x * (10 + w) + 30;
            int cooY = ys[0];
            
            if(outputId == x)
                g.setColor(Color.red);
            else
                g.setColor(Color.black);
            
            g.drawString(String.format("%2.1f", c.getActiveDutyCycle()), cooX, cooY - 10);
            g.drawString(String.format("%2.1f", c.getOverlap()), cooX, cooY - 25);
            g.drawString(String.format("%2.1f", c.getOverlapDutyCycle()), cooX, cooY - 40);
            g.drawString(String.format("%2.1f", c.getMinimalLocalActivity()), cooX, cooY - 55);
            if(c.isActive())
            {
                g.fillOval(cooX, cooY, w, h);
                
                for(InputSynapse s : c.getSynapses())
                {
                    if(sp.isConnected(s) && (s.getInputValue() >= 1 || outputId == x))
                        g.drawLine(cooX + w / 2, cooY + h, s.getInputIndex() * (10 + w) + 30 + w / 2, ys[1]);
                }
            }
            else
            {
                g.drawOval(cooX, cooY, w, h);
                if(outputId == x)
                {
                    for(InputSynapse s : c.getSynapses())
                    {
                        if(sp.isConnected(s))
                            g.drawLine(cooX + w / 2, cooY + h, s.getInputIndex() * (10 + w) + 30 + w / 2, ys[1]);
                    }
                }
            }
            
            if(tp != null && tp.isPreditive(c))
            {
                g.setColor(Color.CYAN);
                g.fillOval(cooX + 5, cooY + 5, w - 5 * 2, h - 5 * 2);
            }
        }
        
        g.setColor(Color.black);
        
        int[] input = inputs[inputIndex % maxInputs];
        for(int x = 0; x < input.length; x++)
        {
            if(inputId == x)
            {
                g.setColor(Color.red);
                
                for(int x2 = 0; x2 < sp.getColumns().length; x2++)
                {
                    Column c = sp.getColumns()[x2];

                    int cooX = x2 * (10 + w) + 30;
                    int cooY = ys[0];

                    //if(c.isActive())
                    {
                        int xx = x;
                        if(c.getSynapses().stream().filter(s -> sp.isConnected(s)/* && s.getInputValue() >= 1*/).anyMatch(s -> s.getInputIndex() == xx))
                        {
                            if(c.isActive())
                                g.fillOval(cooX, cooY, w, h);

                            for(InputSynapse s : c.getSynapses())
                            {
                                if(s.getInputIndex() == xx && sp.isConnected(s)/* && s.getInputValue() >= 1*/)
                                    g.drawLine(cooX + w / 2, cooY + h, s.getInputIndex() * (10 + w) + 30 + w / 2, ys[1]);
                            }
                        }
                    }
                }
            }
            else
                g.setColor(Color.black);
            
            if(input[x] >= 1)
                g.fillOval(x * (10 + w) + 30, ys[1], w, h);
            else
                g.drawOval(x * (10 + w) + 30, ys[1], w, h);
        }
        
        g.setColor(Color.black);
        /*
        for(int x = 0; x < outputSimu.length; x++)
        {
            int c = outputSimu[x];
            
            int cooX = x * (10 + w) + 30;
            int cooY = 300;
            
            if(c == 1)
            {
                g.fillOval(cooX, cooY, w, h);
            }
            else
                g.drawOval(cooX, cooY, w, h);
        }
        
        input = inputSimu;
        for(int x = 0; x < input.length; x++)
        {
            g.setColor(Color.black);
            
            if(input[x] >= 1)
                g.fillOval(x * (10 + w) + 30, 400, w, h);
            else
                g.drawOval(x * (10 + w) + 30, 400, w, h);
        }*/
    }
    
    int inputId = -1;
    int outputId = -1;
    
    
    void computeInputSimu()
    {
        for(int x = 0; x < inputSimu.length; x++)
        {
            int xx = x;
            inputSimu[x] = Stream.of(sp.getColumns())
                    .anyMatch(c -> sp.shouldBeActiveInput(c, xx)) ? 1 : 0;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        Point p = e.getPoint();
        
        int id = (p.x - 30) / (10 + w);
        
        if(p.y > ys[0] && p.y < ys[0] + h)
        { // neur
            inputId = -1;
            outputId = id;
        }
        else if(p.y > ys[1] && p.y < ys[1] + h)
        { // input
            inputId = id;
            outputId = -1;
        }
        else if(p.y > ys[2] && p.y < ys[2] + h)
        { // neur
            inputId = -1;
            outputId = -1;
            
            if(id >= 0 && id < outputSimu.length)
                outputSimu[id] = outputSimu[id] == 0 ? 1 : 0;
            
        }
        else if(p.y > ys[3] && p.y < ys[3] + h)
        { // input
            inputId = id;
            outputId = -1;
        }
        else
        {
            inputId = -1;
            outputId = -1;
        }
            computeInputSimu();
        
        this.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) { }
}
