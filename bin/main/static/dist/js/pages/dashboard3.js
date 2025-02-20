$(function () {
  'use strict';

  var ticksStyle = {
    color: '#495057', // 텍스트 색상
    weight: 'bold' // 텍스트 굵기 (Chart.js 3.x 이상에서는 weight 사용)
  };

  var mode = 'index';
  var intersect = true;

  const dailyLabels = /*[[${dailyLabels}]]*/ [];
  const dailyData = /*[[${dailyData}]]*/ [];
  const lastWeekData = /*[[${lastWeekData}]]*/ [];
  const monthlyLabels = /*[[${monthlyLabels}]]*/ [];
  const monthlyData = /*[[${monthlyData}]]*/ [];

  const labels = dailyLabels;
  const dailyAmounts = dailyData;
  const lastWeekAmounts = lastWeekData;

  const maxdailyAmount = Math.max(...dailyAmounts);
  const maxlastWeekAmount = Math.max(...lastWeekAmounts);
  const maxAmount = Math.max(maxdailyAmount, maxlastWeekAmount);
  
  const suggestedMaxAmount = maxAmount + 100000; // 최대값에 여유 추가

  const ctx_line = document.getElementById('lineChart').getContext('2d');

  const monthlyLabel = monthlyLabels; 
  const monthlyAmounts = monthlyData;

  console.log("Labels from server:", monthlyLabel);
  console.log("Amounts from server:", monthlyAmounts);

  if (monthlyLabel.length === 0 || monthlyAmounts.length === 0) {
	console.warn("No data available for the chart.");
	return;
	}

	const maxMonthlyAmount = Math.max(...monthlyAmounts);
	const suggestedMaxMonthly = maxMonthlyAmount + 100000;

	new Chart(ctx_line, {
		type: 'line',
		data: {
			labels: monthlyLabel,
			datasets: [{
				label: '월별 매출액 (원)', // 오타 수정 (abel → label)
				data: monthlyAmounts,
				backgroundColor: 'rgba(54, 162, 235, 0.5)',
				borderColor: 'rgba(54, 162, 235, 1)',
				borderWidth: 1
			}]
		},
		options: {
			responsive: true,
			scales: {
				y: {
					beginAtZero: true,
					suggestedMax: suggestedMaxMonthly,
					ticks: { // ticks 내부로 callback 이동
						callback(value) {
							return value.toLocaleString() + '원'; 
						}
					}
				}
			}
		}
	});

	var $visitorsChart = $('#visitors-chart');
	new Chart($visitorsChart, {
	  type: 'line',
	  data: {
	    labels: labels,
	    datasets: [
	      {
	        label: '이번 주 매출',
	        data: dailyAmounts,
	        backgroundColor: 'transparent',
	        borderColor: '#007bff',
	        pointBorderColor: '#007bff',
	        pointBackgroundColor: '#007bff',
	        fill: false
	      },
	      {
	        label: '지난 주 매출',
	        data: lastWeekAmounts,
	        backgroundColor: 'transparent',
	        borderColor: '#ced4da',
	        pointBorderColor: '#ced4da',
	        pointBackgroundColor: '#ced4da',
	        fill: false
	      }
	    ]
	  },
	  options: {
	    responsive: true,
	    plugins: {
	      tooltip: {
	        callbacks: {
	          label(context) {
	            let value = context.raw || 0;
	            return value.toLocaleString() + '원';
	          }
	        }
	      },
	      legend: {
	        display: true,
	        labels: {
	          color:'#495057', 
	          font:{
	            weight:'bold'
	         }}
	      }
	    },
	    scales:{
	      y:{
	        beginAtZero:true,
	        ticks:{
	          callback:function(value){
	            return value.toLocaleString()+'원';
	          }
	        }
	      }
	    }
	  }
	});
});
