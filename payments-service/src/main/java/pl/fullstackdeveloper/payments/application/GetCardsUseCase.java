package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;

public interface GetCardsUseCase {

    ResultPage<CardView> handle(PageSpec pageSpec);

}
