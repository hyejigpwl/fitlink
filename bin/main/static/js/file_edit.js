document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('fileInput');
    const addFilesBtn = document.getElementById('addFilesBtn');
    const fileList = document.getElementById('fileList');
    const form = document.getElementById('facilityEdit');
    const MAX_FILES = 4; // 최대 업로드 가능한 파일 개수

    let uploadedFiles = []; // 새로 추가된 파일 목록
    let existingFiles = []; // 기존에 저장된 파일 목록
    let deletedFiles = []; // 삭제된 기존 파일 목록
    let removedNewFiles = []; // 삭제된 새로 추가된 파일 목록(저장 전 리스트에서 보이는 파일 삭제)

    // **1. 초기화: 이미 렌더링된 기존 파일 목록 가져오기**
    document.querySelectorAll('#fileList tr').forEach(row => {
        const fileName = row.querySelector('td').innerText.trim();
        const removeButton = row.querySelector('.remove-file');

        if (fileName && removeButton) {
            removeButton.setAttribute('data-file', fileName);
            existingFiles.push(fileName); // 기존 파일 목록에 추가
        }
    });

    // **2. 리스트 업데이트 함수**
    function updateFileList() {
        fileList.innerHTML = ""; // 리스트 초기화

        // 기존 파일 렌더링
        existingFiles.forEach(fileName => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${fileName}</td>
                <td class="text-right">
                    <button type="button" class="btn btn-danger btn-sm remove-file" data-file="${fileName}" data-column="true">Remove</button>
                </td>
            `;
            fileList.appendChild(row);
        });

        // 새로 추가된 파일 렌더링
        uploadedFiles.forEach(file => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${file.name}</td>
                <td class="text-right">
                    <button type="button" class="btn btn-danger btn-sm remove-file" data-file="${file.name}">Remove</button>
                </td>
            `;
            fileList.appendChild(row);
        });

        attachRemoveHandlers(); // 삭제 버튼 이벤트 핸들러 재설정
    }

    // **3. 파일 추가 버튼 클릭 이벤트**
    addFilesBtn.addEventListener('click', () => {
        if (uploadedFiles.length + existingFiles.length >= MAX_FILES) { // 기존 및 새로 추가된 파일 개수 확인
            alert("최대 4개의 파일만 업로드할 수 있습니다.");
            return;
        }
        fileInput.click();
    });

    // **4. 파일 선택 시 리스트에 추가**
    fileInput.addEventListener('change', () => {
		
        const files = Array.from(fileInput.files); // 사용자가 선택한 파일 배열로 변환

        if (uploadedFiles.length + existingFiles.length + files.length > MAX_FILES) { 
            alert(`최대 ${MAX_FILES}개의 파일만 업로드할 수 있습니다.`);
            fileInput.value = ""; // 입력 초기화
            return;
        }

        files.forEach(file => {
            if (!uploadedFiles.some(uploadedFile => uploadedFile.name === file.name)) { 
                uploadedFiles.push(file); // 새로 추가된 파일 목록에 추가
            } else {
                alert(`${file.name}은(는) 이미 추가된 파일입니다.`);
            }
        });

        updateFileList(); 
    });

    // **5. 삭제 버튼 이벤트 핸들러 추가**
    function attachRemoveHandlers() {
        document.querySelectorAll('.remove-file').forEach(button => {
            button.addEventListener('click', (e) => {
                const fileName = e.target.getAttribute('data-file'); 
                const columnName = e.target.getAttribute('data-column'); 

                if (columnName) { 
                    if (!deletedFiles.includes(fileName)) { 
                        deletedFiles.push(fileName); 
                        existingFiles = existingFiles.filter(file => file !== fileName); 
                    }
                } else { 
                    uploadedFiles = uploadedFiles.filter(file => file.name !== fileName); 
                    removedNewFiles.push(fileName);
                }

                updateFileList(); 
            });
        });
    }

    // **6. 폼 제출 이벤트 처리**
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        console.log("Form submission started");

        const formData = new FormData(form);

        existingFiles.forEach((fileName) => {
            formData.append('existingFiles[]', fileName);
            console.log(`Added to FormData (existing): ${fileName}`);
        });

        uploadedFiles.forEach((file) => {
            formData.append('newFiles[]', file);
            console.log(`Added to FormData (new): ${file.name}`);
        });

        deletedFiles.forEach((fileName) => {
            formData.append('deletedFiles[]', fileName);
            console.log(`Added to FormData (deleted): ${fileName}`);
        });

        removedNewFiles.forEach(fileName => {
            formData.append('removedNewFiles[]', fileName);
            console.log(`Added to removedNewFiles(new): ${fileName}`);
        });

		try {
		        const response = await fetch(form.action, {
		            method: 'POST',
		            body: formData
		        });

		        // JSON 응답 처리
		        const data = await response.json();
		        
		        if (data.redirectUrl) {
		            // 리다이렉션 URL이 있으면 해당 페이지로 이동
					alert("시설 정보가 성공적으로 수정되었습니다.");
		            window.location.href = data.redirectUrl;
		        } else if (response.ok) {
		            alert("시설 정보가 성공적으로 수정되었습니다.");
		        } else {
		            alert("시설 정보 수정 중 오류가 발생했습니다.");
		        }
		    } catch (error) {
		        console.error("Error submitting form:", error);
		        alert("서버와 통신 중 오류가 발생했습니다.");
		    }

		console.log("Form submission ended");
    });

    updateFileList(); 
});
