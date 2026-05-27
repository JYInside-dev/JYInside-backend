package org.najeungyoon.communitybackend.model.category;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.najeungyoon.communitybackend.domain.app.Board;
import org.najeungyoon.communitybackend.domain.app.Category;

public record CategoryCreateResponseDTO(
        @NotBlank(message = "카테고리 이름을 설정해 주세요.")
        String categoryName,

        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        int displayOrder
) {


    public Category toEntity(Board board){
        return new Category(board,this.categoryName,this.displayOrder);
    }
}
