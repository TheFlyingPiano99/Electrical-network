package network;

public class Resistance extends Component {
	public Resistance() {
		resistance = 1.0F;
		current = 0.0F;
		sourceVoltage = 0.0F;
	}

	public Resistance(float r) {
		resistance = r;
		current = 0.0F;
		sourceVoltage = 0.0F;
	}
	
}
