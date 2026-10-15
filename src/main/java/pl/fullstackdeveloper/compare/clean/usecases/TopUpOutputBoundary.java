package pl.fullstackdeveloper.compare.clean.usecases;

// Output Boundary - interaktor nie zwraca wyniku, tylko przekazuje go prezenterowi (przepływ sterowania vs zależności)
public interface TopUpOutputBoundary {

    void present(TopUpResponse response);

}
