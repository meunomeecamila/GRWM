package model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassificarRoupasIA {
private List<Prediction> predictions; //lista de previsao
	
	public ClassificarRoupasIA() {}
	
	//getters e setters
	public List<Prediction> getPredictions(){
		return predictions;
	}
	
	public void setPredictions(List<Prediction> predictions) {
		this.predictions = predictions;
	}
}
