package org.najeungyoon.communitybackend.model.category;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Category;

public record CategoryCreateResponseDTO(
        @NotBlank(message = "카테고리 이름을 설정해 주세요.")
        String categoryName

) {


    public Category toEntity(Board board,int displayOrder){
        return new Category(board,this.categoryName, displayOrder);
    }
}
