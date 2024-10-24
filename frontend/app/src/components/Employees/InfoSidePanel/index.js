import React from 'react';

require('./info-side-panel.scss');

function InfoSidePanel() {
  /* eslint-disable */
  const body = 'Manage your team';
  /* eslint-enable */

  return (
    <div className="employee-side-panel">
      <h3>Employees</h3>
      <p>{body}</p>
    </div>
  );
}

export default InfoSidePanel;
