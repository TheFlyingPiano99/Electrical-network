package network;

public class Wire extends Component {
	public Wire() {
		resistance = 0.001F;
		current = 0.0F;
		sourceVoltage = 0.0F;
	}
	
	public Wire(float r, float i, float u) {
		super(r, i ,u);
	}
}
