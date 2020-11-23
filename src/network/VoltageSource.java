package network;

public class VoltageSource extends Component {
	
	public VoltageSource() {
		resistance = 10.0F;
		current = 0.0F;
		sourceVoltage = 10.0F;
	}
	
	public VoltageSource(float u) {
		resistance = 1.0F;
		current = 0.0F;
		sourceVoltage = u;
	}
	
	public VoltageSource(float r, float i, float u) {
		super(r, i ,u);
	}
	
}
