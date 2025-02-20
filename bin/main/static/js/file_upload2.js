document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const addFilesBtn = document.getElementById('addFilesBtn');
    const form = document.getElementById('facilityAdd'); // 폼 ID
	const fileList = document.getElementById('fileList'); // 파일 리스트 테이블

    let uploadedFiles = []; // 업로드된 파일 목록

    // 파일 추가 버튼 클릭 이벤트
    addFilesBtn.addEventListener('click', () => {
        if (uploadedFiles.length >= 4) {
            alert("최대 4개의 파일만 업로드할 수 있습니다.");
            return;
        }
        fileInput.click();
    });

    // 파일 선택 이벤트
   fileInput.addEventListener('change', () => {
       const files = Array.from(fileInput.files);

       files.forEach(file => {
           if (uploadedFiles.some(uploadedFile => uploadedFile.name === file.name)) {
               alert(`파일 ${file.name}은 이미 추가되었습니다.`);
               return;
           }
           uploadedFiles.push(file);

           // UI에 업로드된 파일 표시
           const fileList = document.getElementById('fileList');
           const row = document.createElement('tr');
           row.innerHTML = `
               <td>${file.name}</td>
               <td class="text-right">
                   <button type="button" class="btn btn-danger btn-sm remove-file" data-file="${file.name}">Remove</button>
               </td>
           `;
           fileList.appendChild(row);
       });

        // Remove 버튼 클릭 핸들러 추가
        document.querySelectorAll('.remove-file').forEach(button => {
            button.addEventListener('click', (e) => {
                const fileName = e.target.getAttribute('data-file');
                uploadedFiles = uploadedFiles.filter(file => file.name !== fileName);
                e.target.closest('tr').remove();
            });
        });
    });

    // 폼 제출 이벤트
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // FormData 객체 생성
        const formData = new FormData(form);

        // 업로드된 파일 추가
        uploadedFiles.forEach(file => {
            formData.append('files[]', file); // 'files'는 서버에서 받을 필드 이름
        });

        try {
            // 서버로 FormData 전송
            const response = await fetch('/admin/Facility_add', {
                method: 'POST',
                body: formData // FormData를 그대로 전송
            });

            if (response.redirected) {
                // 리다이렉션 처리
                window.location.href = response.url;
            } else if (response.ok) {
                alert('성공적으로 제출되었습니다.');
            } else {
                alert('오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 통신 중 오류가 발생했습니다.');
        }
    });
	
	

	
	
	
});