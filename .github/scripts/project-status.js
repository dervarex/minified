const PROJECT = 3;

module.exports = async ({ github, context, status }) => {
  const { user } = await github.graphql(`query($login: String!, $number: Int!) {
    user(login: $login) {
      projectV2(number: $number) {
        id
        field(name: "Status") { ... on ProjectV2SingleSelectField { id options { id name } } }
      }
    }
  }`, { login: context.repo.owner, number: PROJECT });
  const project = user.projectV2;
  const option = project.field.options.find((o) => o.name === status);
  if (!option) {
    throw new Error(`The project has no status "${status}"`);
  }

  // returns the existing item when the issue is already on the project
  const { addProjectV2ItemById } = await github.graphql(`mutation($project: ID!, $content: ID!) {
    addProjectV2ItemById(input: { projectId: $project, contentId: $content }) { item { id } }
  }`, { project: project.id, content: context.payload.issue.node_id });

  await github.graphql(`mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
    updateProjectV2ItemFieldValue(input: {
      projectId: $project, itemId: $item, fieldId: $field, value: { singleSelectOptionId: $option }
    }) { projectV2Item { id } }
  }`, { project: project.id, item: addProjectV2ItemById.item.id, field: project.field.id, option: option.id });
};
