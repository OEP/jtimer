package JTimer;


import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import java.lang.Thread;
import java.util.Vector;

public class JTimer extends MIDlet implements CommandListener {
    private Command exitCommand, startCommand, resetCommand, stopCommand, lapCommand;
    private TimeCanvas mc;

    public JTimer() {
        exitCommand = new Command("Exit", Command.EXIT, 1);
        startCommand = new Command("Start",Command.ITEM,1);
        resetCommand = new Command("Reset",Command.EXIT,1);
        stopCommand = new Command("Stop",Command.ITEM,1);
        lapCommand = new Command("Lap",Command.EXIT,1);
        
        
    }

    protected void startApp() {
    	mc = new TimeCanvas();
        mc.addCommand(exitCommand);
        mc.addCommand(startCommand);
        mc.setCommandListener(this);
        Display.getDisplay(this).setCurrent(mc);

    }

    protected void pauseApp() {
    	System.out.println("PauseAPP called.");
    	mc.pause();
    }
    protected void destroyApp(boolean bool) {}

    public void commandAction(Command cmd, Displayable disp) {
    	
    	
    	if(cmd == startCommand) {
    		mc.start();
    		removeCommands();
    		mc.addCommand(lapCommand);
    		mc.addCommand(stopCommand);
    	}
    	if(cmd == stopCommand) {
    		mc.stop();
    		removeCommands();
    		mc.addCommand(resetCommand);
    		mc.addCommand(startCommand);
    	}
    	if(cmd == resetCommand) {
    		mc.reset();
    		removeCommands();
    		mc.addCommand(exitCommand);
    		mc.addCommand(startCommand);
    	}
    	if(cmd == lapCommand) {
    		mc.lap();
    	}
    	if(cmd == exitCommand) {
    		destroyApp(false);
    		notifyDestroyed();
    	}
    		
    }
    	
    private void removeCommands() {
    	mc.removeCommand(startCommand);
    	mc.removeCommand(stopCommand);
    	mc.removeCommand(exitCommand);
    	mc.removeCommand(resetCommand);
    	mc.removeCommand(lapCommand);
    }
}

class TimeCanvas extends Canvas implements Runnable {
	private static long sum;
	private static long start = 0;
	private static long stop = 0;
	private static long ms;
	private static long s;
	private static long m;
	private static long h;
	private static boolean running = false;
	private static boolean reset = true;
	private static int offset = 0;
	private static Vector laps = new Vector();
	private static Thread th;

	TimeCanvas() {
		start = 0;
		stop = 0;
		th = new Thread(this);
		th.start();
	}
	
	public void pause() {
		th.interrupt();
	}
	
	public void paint(Graphics g) {
		String text = getFormattedTime();
		
		Font big = g.getFont();
		
		g.setColor(255,255,255);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(0,0,0);
		
		// > 0
		for(int i = 0; (i * (big.getHeight() + 1)) < getHeight(); i++) {
			int y = (i * (big.getHeight() + 1));
			int index = (laps.size() - i + offset); //1 equal to lines beforehand
			switch(i) {
			case 0:
				g.drawString(text, getWidth() / 20, y, Graphics.TOP | Graphics.LEFT);
				break;
			
			default:
				if(index >= 0) {
					String lap = new String((index + 1) + ". ");
					lap += laps.elementAt(index);
					g.drawString(lap, getWidth() / 10, (i * (big.getHeight() + 1)), Graphics.TOP | Graphics.LEFT);
				}
			}
		}
	}
	
	public void scrollUp() {
		if(offset < 0) {
			offset++;
		}
			
	}
	
	public void scrollDown() {
		if(laps.size() + offset > 1) {
			offset--;
			
		}
	}
	
	public void start() {
		reset = false;
		if(!running) {
			start = System.currentTimeMillis();
			running = true;
		}
	}
	
	public void stop() {
		if(running) {
			stop = System.currentTimeMillis();
			sum += stop - start;
			running = false;
		}
	}
	
	public void reset() {
		start = 0;
		stop = 0;
		sum = 0;
		offset = 0;
		laps.removeAllElements();
		reset = true;
	}
	
	public void lap() {
		laps.addElement(getFormattedTime());
	}
	
	public boolean isReset() {
		return reset;
	}
	
	public boolean running() {
		return running;
	}
	
	public void updateFigures() {
		if(running)
			ms = (sum + (System.currentTimeMillis() - start)) / 10;
		else
			ms = sum / 10;
		
		//ms += sum;
		
		s = ms / 100; ms -= s * 100;
		m = s / 60; s -= m * 60;
		h = m / 60; m -= h * 60;
		repaint();
	}
	
	public String getFormattedTime() {
		String out = new String(h + ":");
		if(m < 10) out += "0";
		out += m + ":";
		if(s < 10) out += "0";
		out += s + ":";
		if(ms < 10) out += "0";
		out += ms;
		return out;
	}
	
	protected void keyPressed(int k) {
		switch(getGameAction(k)) {
			case UP: scrollUp(); break;
			case DOWN: scrollDown(); break;
		}
	}
	
	protected void keyRepeated(int k) {
		switch(getGameAction(k)) {
			case UP: scrollUp(); break;
			case DOWN: scrollDown(); break;
		}
	}
	
	public void run() {
		try {
			while(true) {
				this.updateFigures();
				Thread.sleep(10);
			}
		}
		catch(Exception e) { System.out.println("Interrupted."); }
		
	}
}